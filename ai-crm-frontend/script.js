const sendBtn = document.getElementById("send-btn");
const chatBox = document.getElementById("chat-box");
const input = document.getElementById("message");
const modal = document.getElementById("confirmModal");
const voiceBtn = document.getElementById("voice-btn");
const historyList = document.getElementById("history-list");

let chatSessions = JSON.parse(
    localStorage.getItem("chatSessions") || "[]"
);

window.onload = () => {
    renderHistory();

    setTimeout(() => {
        addBotMessage(
            "👋 <b>Welcome to AI CRM Pro!</b><br><br>" +
            "I can help you with:<br><br>" +
            "📝 <b>Create Lead</b><br>" +
            "→ create lead name Ravi phone 9876543210 email ravi@gmail.com city Chennai requirement Elevator<br><br>" +
            "📋 <b>Show Leads</b><br>" +
            "→ show leads<br><br>" +
            "🗑 <b>Delete Lead</b><br>" +
            "→ delete Ravi<br><br>" +
            "🏆 <b>Update Deal Status</b><br>" +
            "→ Ravi won / Ravi lost<br><br>" +
            "📅 <b>Follow Up</b><br>" +
            "→ follow up call tomorrow for Ravi<br><br>" +
            "📞 <b>Update Phone</b><br>" +
            "→ change Ravi phone 9999999999<br><br>" +
            "━━━━━━━━━━━━━━━━━━━━<br>" +
            "Type your command or use 🎤 voice!"
        );
    }, 500);
};

// ============================
// USER MESSAGE
// ============================
function addMessage(text, type) {
    const div = document.createElement("div");
    div.className = type;
    div.innerText = text;
    chatBox.appendChild(div);
    chatBox.scrollTop = chatBox.scrollHeight;
}

// ============================
// BOT MESSAGE
// ============================
function addBotMessage(text) {
    const div = document.createElement("div");
    div.className = "bot";
    div.innerHTML = text.replace(/\n/g, "<br>");
    chatBox.appendChild(div);
    chatBox.scrollTop = chatBox.scrollHeight;
}

// ============================
// SEND MESSAGE
// ============================
async function sendMessage() {
    const msg = input.value.trim();

    if (msg === "") return;

    addMessage(msg, "user");
    saveToHistory(msg);
    input.value = "";

    addBotMessage("⏳ Thinking...");

    try {
        const res = await fetch(
            "http://localhost:8080/api/chat/message",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    message: msg,
                    userId: "1",
                    sessionId: "session1"
                })
            }
        );

        const data = await res.json();

        chatBox.lastChild.remove();

        addBotMessage(data.reply);

    } catch (error) {
        chatBox.lastChild.remove();
        addBotMessage("❌ Server not responding.");
        console.error(error);
    }
}

// ============================
// LOAD LEADS (UPDATED)
// ============================
async function loadLeads() {
    addBotMessage("🔄 Loading leads...");

    try {
        const res = await fetch(
            "http://localhost:8080/api/leads/all",
            {
                method: "GET",
                headers: {
                    "Content-Type": "application/json"
                }
            }
        );

        if (!res.ok) {
            addBotMessage("❌ Server error: " + res.status);
            return;
        }

        const leads = await res.json();

        if (!leads || leads.length === 0) {
            addBotMessage("📭 No leads found.");
            return;
        }

        let html =
            "📋 <b>Total Leads: " +
            leads.length +
            "</b><br><br>";

        leads.forEach((lead, index) => {

            html += "━━━━━━━━━━━━━━━━━━━━<br>";
            html += "🔢 <b>Lead #" + (index + 1) + "</b><br>";
            html += "👤 Name       : " + (lead.name || "-") + "<br>";
            html += "📞 Phone      : " + (lead.phone || "-") + "<br>";
            html += "📧 Email      : " + (lead.email || "-") + "<br>";
            html += "🏙 City       : " + (lead.city || "-") + "<br>";
            html += "📄 Requirement: " + (lead.requirement || "-") + "<br>";
            html += "📌 Source     : " + (lead.source || "-") + "<br>";
            html += "🔄 Status     : " + (lead.status || "-") + "<br>";
            html += "💼 Deal       : " + (lead.dealStatus || "PENDING") + "<br>";
            html += "📅 Follow Up  : " + (lead.followUp || "None") + "<br>";
            html += "🕐 Created    : " +
                (lead.createdAt
                    ? lead.createdAt.substring(0, 10)
                    : "-") +
                "<br><br>";
        });

        html += "━━━━━━━━━━━━━━━━━━━━";

        addBotMessage(html);

    } catch (e) {
        addBotMessage("❌ Error: " + e.message);
    }
}

// ============================
// CLEAR CHAT
// ============================
function showClearConfirm() {
    modal.style.display = "flex";
}

function hideClearConfirm() {
    modal.style.display = "none";
}

function clearChat() {
    chatBox.innerHTML = "";
    hideClearConfirm();
}

// ============================
// DARK / LIGHT MODE
// ============================
function toggleMode() {
    document.body.classList.toggle("dark");
    document.body.classList.toggle("light");
}

// ============================
// VOICE
// ============================
function startVoice() {
    const SpeechRecognition =
        window.SpeechRecognition ||
        window.webkitSpeechRecognition;

    if (!SpeechRecognition) {
        addBotMessage("❌ Use Chrome for voice!");
        return;
    }

    navigator.mediaDevices.getUserMedia({
        audio: true
    })
    .then(() => {

        const recognition =
            new SpeechRecognition();

        recognition.lang = "en-US";
        recognition.continuous = false;
        recognition.interimResults = false;

        recognition.onstart = () => {
            voiceBtn.style.background = "#ff4757";
            input.placeholder = "🎤 Listening...";
        };

        recognition.onresult = (event) => {
            const transcript =
                event.results[0][0].transcript;

            input.value = transcript;

            voiceBtn.style.background = "";
            input.placeholder =
                "Type or use voice...";

            setTimeout(() => {
                sendMessage();
            }, 500);
        };

        recognition.onerror = (event) => {

            voiceBtn.style.background = "";
            input.placeholder =
                "Type or use voice...";

            if (event.error === "not-allowed") {
                addBotMessage("❌ Allow mic permission!");
            } else if (
                event.error === "no-speech"
            ) {
                addBotMessage("⚠️ No speech detected!");
            }
        };

        recognition.onend = () => {
            voiceBtn.style.background = "";
            input.placeholder =
                "Type or use voice...";
        };

        recognition.start();
    })
    .catch(() => {
        addBotMessage("❌ Mic permission denied!");
    });
}

// ============================
// EMAIL VALIDATION
// ============================
function isValidEmail(email) {
    const pattern =
        /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;

    return pattern.test(email);
}

// ============================
// SAVE HISTORY
// ============================
function saveToHistory(userMsg) {

    const session = {
        id: Date.now(),
        title:
            userMsg.substring(0, 30) + "...",
        time:
            new Date().toLocaleTimeString()
    };

    chatSessions.unshift(session);

    if (chatSessions.length > 10) {
        chatSessions =
            chatSessions.slice(0, 10);
    }

    localStorage.setItem(
        "chatSessions",
        JSON.stringify(chatSessions)
    );

    renderHistory();
}

// ============================
// RENDER HISTORY
// ============================
function renderHistory() {

    historyList.innerHTML = "";

    if (chatSessions.length === 0) {
        historyList.innerHTML =
            `<div class="history-item">
                No history yet
            </div>`;
        return;
    }

    chatSessions.forEach(session => {

        const div =
            document.createElement("div");

        div.className = "history-item";

        div.innerHTML = `
            <i class="far fa-comment"></i>
            <span>${session.title}</span>
            <small>${session.time}</small>
        `;

        historyList.appendChild(div);
    });
}

// ============================
// CLEAR HISTORY
// ============================
function clearHistory() {
    chatSessions = [];
    localStorage.removeItem(
        "chatSessions"
    );
    renderHistory();
}

// ============================
// EVENTS
// ============================
input.addEventListener(
    "keypress",
    (e) => {
        if (e.key === "Enter") {
            sendMessage();
        }
    }
);

sendBtn.addEventListener(
    "click",
    sendMessage
);