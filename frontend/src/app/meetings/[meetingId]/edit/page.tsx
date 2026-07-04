import { EditMeetingClient } from "./EditMeetingClient";

type EditMeetingPageProps = {
  params: Promise<{ meetingId: string }>;
};

export default async function EditMeetingPage({ params }: EditMeetingPageProps) {
  const { meetingId } = await params;

  return <EditMeetingClient meetingId={meetingId} />;
}
