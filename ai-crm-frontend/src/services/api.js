// src/services/api.js

// Ithu oru Mock API (Dummy API). Backend ready aanathum itha maathikalam.
const api = {
    post: async (url, data) => {
        return new Promise((resolve) => {
            setTimeout(() => {
                resolve({ data: { reply: "Process completed successfully via system. How else can I help you?" } });
            }, 1500); // 1.5 seconds loading aagi reply varum
        });
    },
    get: async (url) => {
        return new Promise((resolve) => {
            setTimeout(() => {
                resolve({ 
                    data: [
                        { name: "Demo Customer", phone: "9876543210", email: "demo@gmail.com", requirement: "AI CRM Software", status: "In Progress", dealStatus: "Open", followUp: "Tomorrow" }
                    ] 
                });
            }, 1000);
        });
    }
};

export default api;