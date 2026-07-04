#!/usr/bin/env node

const DEFAULT_BASE_URL = "https://growth-archive-api.onrender.com/api/v1";
const DEFAULT_MONTH = new Date().toISOString().slice(0, 7);

const baseUrl = trimTrailingSlash(readArg("--base-url") ?? process.env.API_BASE_URL ?? DEFAULT_BASE_URL);
const runs = Math.max(1, Number(readArg("--runs") ?? process.env.RUNS ?? "3"));
const month = readArg("--month") ?? process.env.MONTH ?? DEFAULT_MONTH;
const monthDate = month.length === 7 ? `${month}-01` : month;
const timeoutMs = Math.max(1000, Number(readArg("--timeout-ms") ?? process.env.TIMEOUT_MS ?? "15000"));

const discovered = {
  bookId: null,
  meetingId: null,
  memberId: null,
  reviewId: null,
  readingRecordId: null,
};

const staticEndpoints = [
  endpoint("health", "GET", "/health", "PUBLIC"),
  endpoint("auth.me", "GET", "/auth/me", "PUBLIC"),
  endpoint("library", "GET", "/library", "PUBLIC"),
  endpoint("interest-tags", "GET", "/interest-tags", "PUBLIC"),
  endpoint("books", "GET", "/books", "PUBLIC"),
  endpoint("books.search", "GET", "/books/search?query=%EB%B6%80%EC%9E%90", "PUBLIC"),
  endpoint("reading-records", "GET", "/reading-records", "PUBLIC"),
  endpoint("meetings", "GET", "/meetings", "PUBLIC"),
  endpoint("reviews", "GET", "/reviews", "PUBLIC"),
  endpoint("people", "GET", "/people", "PUBLIC"),
  endpoint("people.monthly-action-plans", "GET", `/people/monthly-action-plans?month=${month}`, "MEMBER"),
  endpoint("onboarding.profile-draft", "GET", "/onboarding/profile-draft", "AUTHENTICATED"),
  endpoint("me.dashboard", "GET", `/me/dashboard?month=${monthDate}`, "MEMBER"),
  endpoint("me.profile", "GET", "/me/profile", "MEMBER"),
  endpoint("me.participation", "GET", `/me/participation/${month}`, "MEMBER"),
  endpoint("me.action-plans", "GET", `/me/action-plans/${month}`, "MEMBER"),
  endpoint("me.reflections", "GET", `/me/reflections/${month}`, "MEMBER"),
  endpoint("admin.dashboard", "GET", "/admin/dashboard", "ADMIN"),
  endpoint("admin.members", "GET", "/admin/members", "ADMIN"),
  endpoint("admin.invite-code", "GET", "/admin/invite-code", "ADMIN"),
  endpoint("admin.books", "GET", "/admin/books", "ADMIN"),
  endpoint("admin.recommended-books", "GET", "/admin/recommended-books", "ADMIN"),
  endpoint("admin.meetings", "GET", "/admin/meetings", "ADMIN"),
  endpoint("admin.participation", "GET", `/admin/participation?month=${month}`, "ADMIN"),
  endpoint("admin.reviews", "GET", "/admin/reviews", "ADMIN"),
  endpoint("admin.interest-tags", "GET", "/admin/interest-tags", "ADMIN"),
];

main().catch((error) => {
  console.error(error);
  process.exit(1);
});

async function main() {
  console.log(`Base URL: ${baseUrl}`);
  console.log(`Runs per endpoint: ${runs}`);
  console.log(`Timeout: ${timeoutMs} ms`);
  console.log("");

  await discoverIds();
  const endpoints = [
    ...staticEndpoints,
    ...dynamicEndpoints(),
  ];

  const results = [];
  for (const target of endpoints) {
    results.push(await measureEndpoint(target));
  }

  printTable(results);
  printSummary(results);
}

async function discoverIds() {
  const [library, meetings, reviews, people] = await Promise.all([
    fetchJson("/library"),
    fetchJson("/meetings"),
    fetchJson("/reviews"),
    fetchJson("/people"),
  ]);

  const libraryData = library?.json?.data;
  discovered.bookId =
    libraryData?.popularBooks?.[0]?.id ??
    libraryData?.recommendedBooks?.[0]?.bookId ??
    libraryData?.recentReadingRecords?.[0]?.bookId ??
    null;
  discovered.readingRecordId = libraryData?.recentReadingRecords?.[0]?.id ?? null;
  discovered.meetingId = meetings?.json?.data?.[0]?.id ?? null;
  discovered.reviewId = reviews?.json?.data?.[0]?.id ?? null;
  discovered.memberId = people?.json?.data?.[0]?.memberId ?? null;
}

function dynamicEndpoints() {
  return [
    discovered.bookId && endpoint("books.detail", "GET", `/books/${discovered.bookId}`, "PUBLIC"),
    discovered.meetingId && endpoint("meetings.detail", "GET", `/meetings/${discovered.meetingId}`, "PUBLIC"),
    discovered.reviewId && endpoint("reviews.detail", "GET", `/reviews/${discovered.reviewId}`, "PUBLIC"),
    discovered.memberId && endpoint("people.detail", "GET", `/people/${discovered.memberId}`, "PUBLIC"),
    discovered.readingRecordId && endpoint("admin.reading-records.hide-target", "GET", `/reading-records?memberId=${discovered.memberId ?? ""}`, "PUBLIC"),
  ].filter(Boolean);
}

async function measureEndpoint(target) {
  const samples = [];
  for (let index = 0; index < runs; index += 1) {
    samples.push(await timedFetch(target.path));
  }
  const okSamples = samples.filter((sample) => sample.durationMs != null);
  return {
    ...target,
    status: samples.map((sample) => sample.status ?? "ERR").join("/"),
    minMs: min(okSamples.map((sample) => sample.durationMs)),
    avgMs: avg(okSamples.map((sample) => sample.durationMs)),
    maxMs: max(okSamples.map((sample) => sample.durationMs)),
    bytes: max(samples.map((sample) => sample.bytes ?? 0)),
    error: samples.find((sample) => sample.error)?.error ?? "",
  };
}

async function fetchJson(path) {
  try {
    const response = await timedFetch(path);
    if (!response.text) {
      return null;
    }
    return {
      ...response,
      json: JSON.parse(response.text),
    };
  } catch {
    return null;
  }
}

async function timedFetch(path) {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), timeoutMs);
  const startedAt = performance.now();
  try {
    const response = await fetch(`${baseUrl}${path}`, {
      method: "GET",
      headers: { Accept: "application/json" },
      redirect: "manual",
      signal: controller.signal,
    });
    const text = await response.text();
    return {
      status: response.status,
      durationMs: performance.now() - startedAt,
      bytes: Buffer.byteLength(text),
      text,
    };
  } catch (error) {
    return {
      durationMs: performance.now() - startedAt,
      error: error instanceof Error ? error.message : String(error),
    };
  } finally {
    clearTimeout(timeout);
  }
}

function endpoint(name, method, path, access) {
  return { name, method, path, access };
}

function printTable(results) {
  const rows = results.map((result) => ({
    endpoint: result.name,
    access: result.access,
    status: result.status,
    avgMs: formatMs(result.avgMs),
    minMs: formatMs(result.minMs),
    maxMs: formatMs(result.maxMs),
    bytes: String(result.bytes),
    error: result.error,
  }));
  console.table(rows);
}

function printSummary(results) {
  const publicResults = results.filter((result) => result.access === "PUBLIC");
  const sorted = [...results].sort((left, right) => (right.avgMs ?? 0) - (left.avgMs ?? 0));
  console.log("");
  console.log(`PUBLIC avg: ${formatMs(avg(publicResults.map((result) => result.avgMs).filter(Boolean)))}`);
  console.log("Slowest endpoints:");
  sorted.slice(0, 5).forEach((result, index) => {
    console.log(`${index + 1}. ${result.name} ${result.path} avg=${formatMs(result.avgMs)} status=${result.status}`);
  });
}

function readArg(name) {
  const prefix = `${name}=`;
  const found = process.argv.find((arg) => arg.startsWith(prefix));
  return found ? found.slice(prefix.length) : null;
}

function trimTrailingSlash(value) {
  return value.replace(/\/$/, "");
}

function formatMs(value) {
  if (value == null || Number.isNaN(value)) {
    return "-";
  }
  return `${Math.round(value)} ms`;
}

function avg(values) {
  const filtered = values.filter((value) => value != null && !Number.isNaN(value));
  if (filtered.length === 0) {
    return null;
  }
  return filtered.reduce((sum, value) => sum + value, 0) / filtered.length;
}

function min(values) {
  const filtered = values.filter((value) => value != null && !Number.isNaN(value));
  return filtered.length ? Math.min(...filtered) : null;
}

function max(values) {
  const filtered = values.filter((value) => value != null && !Number.isNaN(value));
  return filtered.length ? Math.max(...filtered) : null;
}
