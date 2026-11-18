import client from "./client"; // 使用统一的 axios 请求工具，里面已经配置了后端地址
import { cachedTranslate } from "./translateCache";

// 获取个人信息
export async function fetchProfileApi(username) {
  const res = await client.get("/api/profile", {
    params: {
      username: username
    }
  });

  return res.data;
}

// 上传头像
export async function uploadAvatarApi(file) {
  const formData = new FormData();
  formData.append("file", file);

  const res = await client.post("/api/upload", formData, {
    headers: {
      "Content-Type": "multipart/form-data"
    }
  });

  return res.data.url;
}

// 修改个人信息
export async function updateProfileApi(profile) {
  const res = await client.put("/api/profile", profile);

  return res.data;
}

// 翻译文本（带缓存 + 请求去重，避免轮询时刷爆 /api/translate）
export async function translateText(content, targetLang) {
  if (!content) return "";

  if (!targetLang) {
    return content;
  }

  return cachedTranslate(content, targetLang);
}