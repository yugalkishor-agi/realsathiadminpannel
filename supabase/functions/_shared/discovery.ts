import { buildProfilePayload, createAdminClient } from "./auth.ts";

export function stringList(value: unknown): string[] {
  const items = Array.isArray(value) ? value : String(value ?? "").split(",");
  return items.map((item) => String(item ?? "").trim()).filter(Boolean);
}

function overlaps(values: unknown, requested: string[]): boolean {
  if (!requested.length) return true;
  const available = stringList(values).map((item) => item.toLowerCase());
  return requested.some((item) => available.includes(item.toLowerCase()));
}

export async function eligibleHosts(
  userId: string,
  topicTags: string[],
  languages: string[],
  liveOnly: boolean,
) {
  const db = createAdminClient();
  const { data: blocks, error: blocksError } = await db.from("user_blocks")
    .select("blocker_id, blocked_id")
    .or(`blocker_id.eq.${userId},blocked_id.eq.${userId}`);
  if (blocksError) throw blocksError;
  const blocked = new Set((blocks ?? []).map((row) =>
    row.blocker_id === userId ? row.blocked_id : row.blocker_id
  ));

  const { data, error } = await db.from("users").select("*")
    .eq("role", "host").eq("host_status", "approved")
    .limit(100);
  if (error) throw error;

  return (data ?? [])
    .filter((host) => host.id !== userId && !blocked.has(host.id))
    .filter((host) => !liveOnly || host.host_audio_live || host.host_video_live)
    .filter((host) => overlaps(host.topic_tags?.length ? host.topic_tags : host.interests, topicTags))
    .filter((host) => overlaps(host.native_languages?.length ? host.native_languages : host.language, languages))
    .map((host) => {
      const profile = buildProfilePayload(host);
      return {
        id: host.id,
        nickname: profile.nickname,
        username: profile.username,
        avatarUrl: host.host_profile_photo_url || host.avatar_url || null,
        hostProfilePhotoUrl: profile.hostProfilePhotoUrl,
        topicTags: stringList(host.topic_tags?.length ? host.topic_tags : host.interests),
        nativeLanguages: stringList(host.native_languages?.length ? host.native_languages : host.language),
        preferredLanguage: profile.preferredLanguage,
        hostAudioLive: profile.hostAudioLive,
        hostVideoLive: profile.hostVideoLive,
        hostAudioRate: profile.hostAudioRate,
        hostVideoRate: profile.hostVideoRate,
        hostStatus: profile.hostStatus,
      };
    });
}
