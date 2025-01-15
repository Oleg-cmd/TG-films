import dotenv from "dotenv";
import path, { dirname } from "path";
import { fileURLToPath } from "url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

dotenv.config({ path: path.resolve(__dirname, "../.env") });

export const BOT_TOKEN = process.env.BOT_TOKEN;
export const WEBAPP_URL = process.env.WEBAPP_URL;
export const API_URL = process.env.API_URL;
