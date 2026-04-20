import React, { useState, useEffect, useRef, useCallback } from 'react';
import { BrowserRouter, Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  SunMoon, MessageSquare, Users, Eraser, Mic, Send, Trash2, 
  Lock, User, LogOut, Mail, ArrowLeft, ShieldAlert, UserCheck, 
  Briefcase, HeadphonesIcon, FileText, Eye, EyeOff, Menu, X 
} from 'lucide-react';
import Particles from "react-tsparticles";
import { loadSlim } from "tsparticles-slim";
import api from './services/api';
import './index.css';

// ============================
// 1. LOGIN COMPONENT
// ============================
function Login({ setAuth, setUserRole, isDarkMode, setIsDarkMode }) {
  const [view, setView] = useState("login");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [email, setEmail] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [showSignupPassword, setShowSignupPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const navigate = useNavigate();

  const particlesInit = useCallback(async engine => {
    await loadSlim(engine);
  }, []);

  const handleLogin = (e) => {
    e.preventDefault();
    if (username && password) {
      let role = "customer"; 
      const lowerUser = username.toLowerCase().trim();
      
      if (lowerUser === "admin@gmail.com") {
        if (password === "admin@123") { role = "admin"; } 
        else { alert("❌ Incorrect Admin Password!"); return; }
      } else if (lowerUser.includes("sales")) {
        role = "salesman";
      }

      setUserRole(role);
      setAuth(true);
      navigate("/dashboard");
    }
  };

  const handleSignup = (e) => {
    e.preventDefault();
    if (password !== confirmPassword) { alert("Passwords do not match!"); return; }
    alert(`Account created successfully for ${username}! Please log in.`);
    setView("login");
  };

  const handleForgotPassword = (e) => {
    e.preventDefault();
    alert(`Password reset link sent securely to ${email}!`);
    setView("login");
  };

  const formVariants = {
    hidden: { opacity: 0, x: -20 },
    visible: { opacity: 1, x: 0, transition: { duration: 0.3 } },
    exit: { opacity: 0, x: 20, transition: { duration: 0.2 } }
  };

  return (
    <div className="login-layout">
      {/* Ultra-Visible Theme Toggle */}
      <div className="theme-mini-btn floating" style={{ position: 'absolute', top: 24, right: 24, zIndex: 9999 }}>
        <button 
          onClick={() => setIsDarkMode(!isDarkMode)}
          style={{
            display: 'flex', alignItems: 'center', gap: '10px', padding: '10px 20px', borderRadius: '30px',
            background: isDarkMode ? '#1e293b' : '#ffffff', border: '2px solid var(--primary)', 
            boxShadow: '0 0 20px rgba(255, 140, 0, 0.4)', color: isDarkMode ? '#ffffff' : '#0f172a',
            fontSize: '14px', fontWeight: '700', fontFamily: "'Syne', sans-serif", cursor: 'pointer', transition: 'all 0.3s ease',
          }}
        >
          <SunMoon size={20} color="var(--primary)" />
          {isDarkMode ? 'DARK' : 'LIGHT'}
        </button>
      </div>

      <Particles
        id="tsparticles"
        init={particlesInit}
        options={{
          fullScreen: { enable: false, zIndex: 0 },
          background: { color: { value: "transparent" } },
          fpsLimit: 120,
          interactivity: {
            events: { onHover: { enable: true, mode: "repulse" }, resize: true },
            modes: { repulse: { distance: 100, duration: 0.4, speed: 1 } },
          },
          particles: {
            color: { value: ["#ff8c00", "#ffcc00", "#ffffff", "#ff4500"] }, 
            links: { enable: false },
            move: { direction: "top", enable: true, outModes: { default: "out" }, random: true, speed: { min: 2, max: 6 }, straight: false },
            number: { density: { enable: true, area: 800 }, value: 400 },
            opacity: { value: { min: 0.1, max: 1 }, animation: { enable: true, speed: 4, minimumValue: 0 } },
            shape: { type: "circle" },
            size: { value: { min: 0.5, max: 2.5 }, animation: { enable: true, speed: 3, minimumValue: 0.1 } },
          },
          detectRetina: true,
        }}
        style={{ position: "absolute", top: 0, left: 0, width: "100%", height: "100%", zIndex: 0 }}
      />

      <motion.div className="login-card" initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.6, ease: "easeOut" }}>
        <div className="login-header">
          <div className="logo-3d">🤖</div>
          <h2>AI <span>CRM</span></h2>
          <p>
            {view === "login" && "Sign in to your account"}
            {view === "signup" && "Create your premium account"}
            {view === "forgot" && "Recover your account access"}
          </p>
        </div>

        <AnimatePresence mode="wait">
          {view === "login" && (
            <motion.form key="login" variants={formVariants} initial="hidden" animate="visible" exit="exit" className="login-form" onSubmit={handleLogin}>
              <div className="input-group">
                <User size={18} />
                <input type="text" placeholder="Email or Username" value={username} onChange={e => setUsername(e.target.value)} required />
              </div>
              <div className="input-group">
                <Lock size={18} />
                <input type={showPassword ? "text" : "password"} placeholder="Password" value={password} onChange={e => setPassword(e.target.value)} required />
                <button type="button" className="eye-btn" onClick={() => setShowPassword(!showPassword)}>
                  {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
              <div className="form-options">
                <button type="button" className="forgot-password" onClick={() => setView("forgot")}>Forgot Password?</button>
              </div>
              <button type="submit" className="btn-login">Login Securely</button>
              <div className="signup-wrapper">
                Don't have an account? <button type="button" className="signup-link" onClick={() => setView("signup")}>Sign Up</button>
              </div>
            </motion.form>
          )}

          {view === "signup" && (
            <motion.form key="signup" variants={formVariants} initial="hidden" animate="visible" exit="exit" className="login-form" onSubmit={handleSignup}>
              <div className="input-group"><User size={18} /><input type="text" placeholder="Choose a Username" value={username} onChange={e => setUsername(e.target.value)} required /></div>
              <div className="input-group"><Mail size={18} /><input type="email" placeholder="Email Address" value={email} onChange={e => setEmail(e.target.value)} required /></div>
              <div className="input-group">
                <Lock size={18} />
                <input type={showSignupPassword ? "text" : "password"} placeholder="Create Password" value={password} onChange={e => setPassword(e.target.value)} required />
                <button type="button" className="eye-btn" onClick={() => setShowSignupPassword(!showSignupPassword)}>{showSignupPassword ? <EyeOff size={16} /> : <Eye size={16} />}</button>
              </div>
              <div className="input-group">
                <Lock size={18} />
                <input type={showConfirmPassword ? "text" : "password"} placeholder="Confirm Password" value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} required />
                <button type="button" className="eye-btn" onClick={() => setShowConfirmPassword(!showConfirmPassword)}>{showConfirmPassword ? <EyeOff size={16} /> : <Eye size={16} />}</button>
              </div>
              <button type="submit" className="btn-login">Create Account</button>
              <button type="button" className="forgot-password" onClick={() => setView("login")} style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px', marginTop: '10px', width: '100%' }}>
                <ArrowLeft size={14} /> Back to Login
              </button>
            </motion.form>
          )}

          {view === "forgot" && (
            <motion.form key="forgot" variants={formVariants} initial="hidden" animate="visible" exit="exit" className="login-form" onSubmit={handleForgotPassword}>
              <div className="input-group"><Mail size={18} /><input type="email" placeholder="Enter your registered email" value={email} onChange={e => setEmail(e.target.value)} required /></div>
              <button type="submit" className="btn-login">Send Reset Link</button>
              <button type="button" className="forgot-password" onClick={() => setView("login")} style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px', marginTop: '10px', width: '100%' }}>
                <ArrowLeft size={14} /> Back to Login
              </button>
            </motion.form>
          )}
        </AnimatePresence>
      </motion.div>
    </div>
  );
}

// ============================
// 2. DASHBOARD COMPONENT
// ============================
function Dashboard({ setAuth, userRole, isDarkMode, setIsDarkMode }) {
  const [messages, setMessages] = useState([]);
  const [inputValue, setInputValue] = useState("");
  const [chatSessions, setChatSessions] = useState(() => JSON.parse(localStorage.getItem("chatSessions") || "[]"));
  const [showModal, setShowModal] = useState(false);
  const [isListening, setIsListening] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false); // Mobile Menu State
  
  const chatBoxRef = useRef(null);

  const isAdmin = userRole === "admin";
  const isSalesman = userRole === "salesman";
  const isCustomer = userRole === "customer";

  useEffect(() => {
    setTimeout(() => {
      let welcomeText = `👋 <b>Welcome to AI CRM Pro!</b><br><br>`;
      if (isAdmin) {
        welcomeText += "👑 <b>ADMINISTRATOR ACCESS</b><br>Full database control enabled.<br><br>📝 <b>Assign Lead</b> → assign lead Ravi to Salesman_1<br>📋 <b>View DB</b> → show all leads / show all users<br>🗑 <b>Delete</b> → delete lead Ravi<br>⚙️ <b>System</b> → generate business report<br>";
      } else if (isSalesman) {
        welcomeText += "💼 <b>SALESMAN WORKSPACE</b><br>Manage your pipeline & follow-ups.<br><br>📋 <b>My Pipeline</b> → show my leads<br>🏆 <b>Update Status</b> → update Ravi status to Won<br>📅 <b>Add Note</b> → add follow-up for Ravi tomorrow<br>";
      } else {
        welcomeText += "👤 <b>CUSTOMER PORTAL</b><br>How can we assist you today?<br><br>➕ <b>New Request</b> → submit new requirement for Elevator<br>📋 <b>Check Status</b> → show my request status<br>📞 <b>Update Info</b> → update my phone to 9999999999<br>";
      }
      welcomeText += "<br>━━━━━━━━━━━━━━━━━━━━<br>Type your command or use 🎤 voice!";
      addBotMessage(welcomeText);
    }, 500);
  }, [isAdmin, isSalesman, isCustomer]);

  useEffect(() => {
    if (chatBoxRef.current) chatBoxRef.current.scrollTop = chatBoxRef.current.scrollHeight;
  }, [messages]);

  const addMessage = (text, type) => setMessages(prev => [...prev, { id: Date.now() + Math.random(), text, type }]);
  const addBotMessage = (text) => addMessage(text.replace(/\n/g, "<br>"), "bot");

  const saveToHistory = (userMsg) => {
    const session = { id: Date.now(), title: userMsg.substring(0, 30) + "...", time: new Date().toLocaleTimeString() };
    const updatedSessions = [session, ...chatSessions].slice(0, 10);
    setChatSessions(updatedSessions);
    localStorage.setItem("chatSessions", JSON.stringify(updatedSessions));
  };

  const handleSendMessage = async () => {
    const msg = inputValue.trim();
    if (msg === "") return;

    addMessage(msg, "user");
    saveToHistory(msg);
    setInputValue("");

    const thinkingId = Date.now() + Math.random();
    setMessages(prev => [...prev, { id: thinkingId, text: "⏳ Processing securely...", type: "bot" }]);

    try {
      const res = await api.post("/chat/message", { message: msg, userId: "1", sessionId: "session1", role: userRole });
      setMessages(prev => prev.filter(m => m.id !== thinkingId));
      addBotMessage(res.data.reply);
    } catch (error) {
      setMessages(prev => prev.filter(m => m.id !== thinkingId));
      addBotMessage("❌ Server not responding. Check connection.");
    }
  };

  const fetchDataForRole = async () => {
    addBotMessage("🔄 Fetching authorized records...");
    setIsMobileMenuOpen(false); // Close mobile menu after clicking
    try {
      let endpoint = "/leads/my"; 
      if (isAdmin) endpoint = "/leads/all";
      const res = await api.get(`${endpoint}?role=${userRole}`);
      const leads = res.data;
      if (!leads || leads.length === 0) { addBotMessage("📭 No records found in your access level."); return; }
      let html = `📋 <b>Records Found: ${leads.length}</b><br><br>`;
      leads.forEach((lead, index) => {
        html += "━━━━━━━━━━━━━━━━━━━━<br>";
        html += `🔢 <b>Record #${index + 1}</b><br>`;
        html += `👤 Name        : ${lead.name || "-"}<br>`;
        if (isAdmin || isSalesman) {
            html += `📞 Phone       : ${lead.phone || "-"}<br>`;
            html += `📧 Email       : ${lead.email || "-"}<br>`;
            html += `🏙 City        : ${lead.city || "-"}<br>`;
        }
        html += `📄 Requirement: ${lead.requirement || "-"}<br>`;
        html += `🔄 Status      : <b>${lead.status || "Pending"}</b><br>`;
        if (isAdmin || isSalesman) {
            html += `💼 Deal Status : ${lead.dealStatus || "Open"}<br>`;
            html += `📅 Follow Up  : ${lead.followUp || "None"}<br>`;
        }
        if (isAdmin) {
            html += `📌 Source      : ${lead.source || "-"}<br>`;
            html += `👔 Assigned To : ${lead.assignedTo || "Unassigned"}<br>`;
        }
        html += "<br>";
      });
      html += "━━━━━━━━━━━━━━━━━━━━";
      addBotMessage(html);
    } catch (e) { addBotMessage("❌ Access Error: " + e.message); }
  };

  const startVoice = () => {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!SpeechRecognition) { addBotMessage("❌ Use Chrome for voice!"); return; }
    navigator.mediaDevices.getUserMedia({ audio: true }).then(() => {
        const recognition = new SpeechRecognition();
        recognition.lang = "en-US";
        recognition.continuous = false;
        recognition.interimResults = false;
        recognition.onstart = () => setIsListening(true);
        recognition.onresult = (event) => {
          const transcript = event.results[0][0].transcript;
          setInputValue(transcript);
          setIsListening(false);
          setTimeout(() => handleSendMessage(), 500);
        };
        recognition.onerror = (event) => {
          setIsListening(false);
          if (event.error === "not-allowed") { addBotMessage("❌ Allow mic permission!"); } 
          else if (event.error === "no-speech") { addBotMessage("⚠️ No speech detected!"); }
        };
        recognition.onend = () => setIsListening(false);
        recognition.start();
      }).catch(() => { addBotMessage("❌ Mic permission denied!"); });
  };

  return (
    <div className="layout">
      
      {/* Mobile Top Header */}
      <div className="mobile-header">
        <h2>AI <span>CRM</span></h2>
        <div style={{ display: 'flex', gap: '15px' }}>
            <button className="theme-mini-btn" onClick={() => setIsDarkMode(!isDarkMode)} style={{ border:'none', background:'transparent', color:'var(--text-main)' }}>
                <SunMoon size={22} />
            </button>
            <button className="menu-toggle-btn" onClick={() => setIsMobileMenuOpen(true)}>
                <Menu size={26} />
            </button>
        </div>
      </div>

      {/* Mobile Overlay Background */}
      <div 
        className={`mobile-overlay ${isMobileMenuOpen ? 'show' : ''}`} 
        onClick={() => setIsMobileMenuOpen(false)}
      ></div>

      {/* Sidebar (Drawer on Mobile, Fixed on Desktop) */}
      <div className={`sidebar ${isMobileMenuOpen ? 'mobile-open' : ''}`}>
        <div className="sidebar-top">
          <div className="logo-area">
            <div className="logo-3d">🤖</div>
            <h2>AI <span>CRM</span></h2>
            {/* Close button for Mobile inside Sidebar */}
            <button 
                className="menu-toggle-btn" 
                onClick={() => setIsMobileMenuOpen(false)} 
                style={{ marginLeft: 'auto', display: window.innerWidth <= 768 ? 'block' : 'none' }}
            >
                <X size={24} />
            </button>
            {/* Theme Toggle - Desktop Only */}
            <button 
              onClick={() => setIsDarkMode(!isDarkMode)}
              style={{
                width: '36px', height: '36px', borderRadius: '50%', background: 'var(--primary-grad)', border: 'none',
                boxShadow: '0 4px 16px rgba(255, 140, 0, 0.4)', color: '#ffffff', display: window.innerWidth > 768 ? 'flex' : 'none',
                alignItems: 'center', justifyContent: 'center', cursor: 'pointer', marginLeft: 'auto'
              }}
            >
              <SunMoon size={18} />
            </button>
          </div>

          <div style={{ marginBottom: '24px', background: 'rgba(255,140,0,0.1)', padding: '10px 14px', borderRadius: '12px', display: 'flex', alignItems: 'center', gap: '10px', color: 'var(--primary)', fontSize: '13px', fontWeight: 'bold', border: '1px solid rgba(255,140,0,0.2)' }}>
            {isAdmin && <ShieldAlert size={16} />}
            {isSalesman && <Briefcase size={16} />}
            {isCustomer && <HeadphonesIcon size={16} />}
            {userRole.toUpperCase()} PORTAL
          </div>
          
          <div className="history-section">
            <p className="section-label">ACTIVITY LOG</p>
            <div id="history-list">
              {chatSessions.length === 0 ? (
                <div className="history-item">No recent activity</div>
              ) : (
                chatSessions.map(session => (
                  <div key={session.id} className="history-item"><MessageSquare size={14} /><span>{session.title}</span></div>
                ))
              )}
            </div>
          </div>
        </div>
        
        <div className="sidebar-bottom">
          {isAdmin && (
            <>
              <button className="util-btn" onClick={fetchDataForRole}><Users size={16} /> All Leads DB</button>
              <button className="util-btn" onClick={() => { setShowModal(true); setIsMobileMenuOpen(false); }}><Eraser size={16} /> Clear System Chat</button>
            </>
          )}
          {isSalesman && <button className="util-btn" onClick={fetchDataForRole}><FileText size={16} /> My Assigned Pipeline</button>}
          {isCustomer && <button className="util-btn" onClick={fetchDataForRole}><FileText size={16} /> Check My Status</button>}

          <button className="util-btn" onClick={() => setAuth(false)} style={{ marginTop: '10px', color: '#ff4757' }}>
            <LogOut size={16} color="#ff4757" /> Logout
          </button>
        </div>
      </div>

      <div className="main">
        <div id="chat-box" ref={chatBoxRef}>
          <AnimatePresence>
            {messages.map((msg) => (
              <motion.div key={msg.id} className={msg.type} initial={{ opacity: 0, y: 10, scale: 0.97 }} animate={{ opacity: 1, y: 0, scale: 1 }} exit={{ opacity: 0, scale: 0.9 }} transition={{ duration: 0.3, ease: [0.34, 1.56, 0.64, 1] }} dangerouslySetInnerHTML={{ __html: msg.text }} />
            ))}
          </AnimatePresence>
        </div>
        
        <div className="input-container">
          <div className="input-area">
            <input value={inputValue} onChange={(e) => setInputValue(e.target.value)} onKeyDown={(e) => e.key === 'Enter' && handleSendMessage()} placeholder="Enter command or query..." autoFocus autoComplete="off" />
            <div className="action-buttons" style={{ display: 'flex', gap: '6px' }}>
              <button className={`icon-btn ${isListening ? 'listening' : ''}`} onClick={startVoice} style={isListening ? { background: '#ff4757', color: '#fff' } : {}}>
                <Mic size={18} />
              </button>
              <button className="send-btn" onClick={handleSendMessage}><Send size={16} style={{ marginLeft: '-2px' }} /></button>
            </div>
          </div>
        </div>
      </div>

      <AnimatePresence>
        {showModal && isAdmin && (
          <motion.div className="modal-overlay" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} style={{ display: 'flex' }}>
            <motion.div className="modal-card" initial={{ opacity: 0, scale: 0.88 }} animate={{ opacity: 1, scale: 1 }} exit={{ opacity: 0, scale: 0.88 }} transition={{ duration: 0.25, ease: [0.34, 1.56, 0.64, 1] }}>
              <Trash2 size={35} color="#ff4757" style={{ margin: '0 auto 15px', display: 'block' }} />
              <h3>Wipe Chat History?</h3>
              <div className="modal-actions">
                <button className="btn-cancel" onClick={() => setShowModal(false)}>Cancel</button>
                <button className="btn-confirm" onClick={() => { setMessages([]); setShowModal(false); }}>Clear System Memory</button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}

// ============================
// 3. MAIN APP ROUTER
// ============================
export default function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(() => localStorage.getItem("auth") === "true");
  const [userRole, setUserRole] = useState(() => localStorage.getItem("role") || "customer");
  const [isDarkMode, setIsDarkMode] = useState(true);

  useEffect(() => { document.body.className = isDarkMode ? "dark" : "light"; }, [isDarkMode]);
  useEffect(() => { 
      localStorage.setItem("auth", isAuthenticated); 
      localStorage.setItem("role", userRole);
  }, [isAuthenticated, userRole]);

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={!isAuthenticated ? <Login setAuth={setIsAuthenticated} setUserRole={setUserRole} isDarkMode={isDarkMode} setIsDarkMode={setIsDarkMode} /> : <Navigate to="/dashboard" />} />
        <Route path="/dashboard" element={isAuthenticated ? <Dashboard setAuth={setIsAuthenticated} userRole={userRole} isDarkMode={isDarkMode} setIsDarkMode={setIsDarkMode} /> : <Navigate to="/login" />} />
        <Route path="*" element={<Navigate to={isAuthenticated ? "/dashboard" : "/login"} />} />
      </Routes>
    </BrowserRouter>
  );
}