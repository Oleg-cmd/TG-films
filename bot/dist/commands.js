import axios from "axios";
import { WEBAPP_URL, API_URL } from "./config.js";
export const handleStartCommand = (ctx) => {
    const telegramId = ctx.from.id.toString();
    ctx.reply(`Welcome to Kino Bot! 🚀\nUse /help to see available commands. Your telegramId is ${telegramId}`);
};
export const handleHelpCommand = (ctx) => {
    ctx.reply(`Available commands:\n` +
        `/start - Start the bot\n` +
        `/help - Show this help message\n` +
        `/kino - Open the Mini App`);
};
export const handleKinoCommand = async (ctx) => {
    const telegramId = ctx.from.id.toString();
    try {
        console.log(`Going to ${API_URL}/api/rooms/create`);
        const response = await axios.post(`${API_URL}/api/rooms/create`, {
            telegramId,
        });
        const inviteCode = response.data.inviteCode;
        const webAppUrl = `${WEBAPP_URL}?inviteCode=${inviteCode}&telegramId=${telegramId}`;
        // Кнопка "Поделиться с друзьями"
        ctx.reply(`✨ You are one step away from opening the app! ✨\n` +
            `Choose how you'd like to open it:\n\n` +
            `🌐 Open in Browser or\n📱 Open in Telegram`, {
            reply_markup: {
                inline_keyboard: [
                    [
                        {
                            text: "🌐 Open in Browser",
                            url: webAppUrl,
                        },
                        {
                            text: "📱 Open in Telegram",
                            web_app: {
                                url: webAppUrl,
                            },
                        },
                        {
                            text: "🔗 Share with friends",
                            switch_inline_query: `${webAppUrl}`, // Эта кнопка открывает возможность поделиться ссылкой
                        },
                    ],
                ],
            },
        });
    }
    catch (error) {
        console.log(error);
        ctx.reply("Oops! Something went wrong. Please try again later.");
    }
};
//# sourceMappingURL=commands.js.map