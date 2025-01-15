import dotenv from "dotenv";
import { Telegraf } from "telegraf";
import { handleStartCommand, handleHelpCommand, handleKinoCommand, } from "./commands.js";
import { BOT_TOKEN } from "./config.js";
dotenv.config(); // Загружаем переменные из .env
// Проверка наличия токена
if (!BOT_TOKEN) {
    throw new Error("BOT_TOKEN must be provided!");
}
const bot = new Telegraf(BOT_TOKEN);
// Команды бота
bot.command("start", handleStartCommand);
bot.command("help", handleHelpCommand);
bot.command("kino", handleKinoCommand);
// Запуск бота
bot.launch().then(() => {
    console.log("Bot is running...");
});
// Остановка бота при получении сигнала
process.once("SIGINT", () => bot.stop("SIGINT"));
process.once("SIGTERM", () => bot.stop("SIGTERM"));
//# sourceMappingURL=bot.js.map