"use client";

import { AuthGate } from "@/components/AuthGate";
import { PlaceholderPage } from "@/components/ui/PlaceholderPage";

type AccessLevel = "MEMBER" | "ADMIN";

type ProtectedPlaceholderPageProps = {
  required: AccessLevel;
  eyebrow: string;
  title: string;
  description: string;
};

export function ProtectedPlaceholderPage({ required, eyebrow, title, description }: ProtectedPlaceholderPageProps) {
  return (
    <AuthGate required={required}>
      {() => <PlaceholderPage eyebrow={eyebrow} title={title} description={description} status={`${required} route`} />}
    </AuthGate>
  );
}
