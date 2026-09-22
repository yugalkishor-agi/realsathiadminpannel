import { authenticateRequest, corsResponse, jsonResponse, requestErrorResponse } from "../_shared/auth.ts";
import { eligibleHosts, stringList } from "../_shared/discovery.ts";

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return corsResponse();
  if (req.method !== "GET") return jsonResponse({ message: "Method not allowed." }, 405);
  try {
    const { userId } = await authenticateRequest(req);
    const params = new URL(req.url).searchParams;
    const topicTags = stringList(params.getAll("topicTags"));
    const languages = stringList(params.getAll("languages"));
    const liveOnly = params.get("liveOnly") !== "false";
    const hosts = await eligibleHosts(userId, topicTags, languages, liveOnly);
    return jsonResponse({ hosts, filters: { topicTags, languages, liveOnly } });
  } catch (error) {
    console.error("discovery-hosts error", error);
    return requestErrorResponse(error, "Unable to load hosts right now.");
  }
});
