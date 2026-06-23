import { ProtectedPlaceholderPage } from "@/components/ui/ProtectedPlaceholderPage";

export default function MyProfilePage() {
  return (
    <ProtectedPlaceholderPage
      required="MEMBER"
      eyebrow="Profile"
      title="프로필 수정"
      description="한 줄 소개 80자와 주요 소개 항목 1000자 제한을 적용할 프로필 관리 화면입니다."
    />
  );
}
