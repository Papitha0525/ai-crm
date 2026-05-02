import React, { useState, useEffect, useRef, useCallback } from 'react';
import { BrowserRouter, Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  SunMoon, MessageSquare, Eraser, Mic, Send, Trash2, 
  Lock, User, LogOut, Mail, ArrowLeft, ShieldAlert, 
  Briefcase, HeadphonesIcon, FileText, Eye, EyeOff, Menu, X, Plus, Check,
  Rocket, Database
} from 'lucide-react';
import Particles from "react-tsparticles";
import { loadSlim } from "tsparticles-slim";
import { Toaster, toast } from 'sonner';
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
  const particlesInit = useCallback(async engine => { await loadSlim(engine); }, []);

 const handleLogin = async (e) => {
  e.preventDefault();

  if (!username || !password) {
    toast.error("Enter email and password");
    return;
  }

  const lowerUser = username.toLowerCase().trim();

  // 🔥 1. ADMIN LOGIN (HARDCODE)
  if (lowerUser === "admin@gmail.com" && password === "admin@123") {

    const role = "ADMIN";

    localStorage.setItem("auth", "true");
    localStorage.setItem("role", role);
    localStorage.setItem("token", "admin-token"); // dummy
    localStorage.setItem("userId", "admin@gmail.com");

    setUserRole(role);
    setAuth(true);

    toast.success("Admin login successful");

    navigate("/dashboard");
    return; // 🚨 IMPORTANT (stop here)
  }

  // 🔥 2. NORMAL LOGIN (DB)
  try {
    const res = await api.post("/api/auth/login", {
      email: username,
      password: password
    });

    const role = res.data.role;

    localStorage.setItem("auth", "true");
    localStorage.setItem("role", role);
    localStorage.setItem("token", res.data.token);
    localStorage.setItem("userId", res.data.email);

    setUserRole(role);
    setAuth(true);

    toast.success("Login successful");

    navigate("/dashboard");

  } catch (err) {
    toast.error("Invalid email or password");
  }
};

  const handleSignup = async (e) => {
  e.preventDefault();

  if (password !== confirmPassword) {
    toast.error("Passwords do not match!");
    return;
  }

  try {
    const res = await api.post("/api/auth/register", {
      name: username,
      email: email,
      password: password
    });

    toast.success("Account created successfully!");
    setView("login");

  } catch (err) {
    toast.error("Signup failed");
  }
};

  const handleForgotPassword = (e) => {
    e.preventDefault();
    toast.info('Password reset link sent securely!');
    setView("login");
  };

  const formVariants = { hidden: { opacity: 0, x: -20 }, visible: { opacity: 1, x: 0, transition: { duration: 0.3 } }, exit: { opacity: 0, x: 20, transition: { duration: 0.2 } } };

  return (
    <div className="login-layout">
      <div style={{ position: 'absolute', top: 20, right: 20, zIndex: 9999 }}>
        <button onClick={() => setIsDarkMode(!isDarkMode)} style={{ width: '40px', height: '40px', borderRadius: '50%', background: isDarkMode ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.04)', backdropFilter: 'blur(10px)', border: `1px solid ${isDarkMode ? 'rgba(255, 255, 255, 0.15)' : 'rgba(0,0,0,0.1)'}`, color: 'var(--primary)', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', boxShadow: '0 4px 15px rgba(0,0,0,0.1)', transition: 'all 0.3s ease' }}>
          <SunMoon size={18} />
        </button>
      </div>

      <Particles id="tsparticles" init={particlesInit} options={{ fullScreen: { enable: false, zIndex: 0 }, background: { color: { value: "transparent" } }, fpsLimit: 120, interactivity: { events: { onHover: { enable: true, mode: "repulse" }, resize: true }, modes: { repulse: { distance: 100, duration: 0.4, speed: 1 } } }, particles: { color: { value: ["#ff8c00", "#ffcc00", "#ffffff", "#ff4500"] }, links: { enable: false }, move: { direction: "top", enable: true, outModes: { default: "out" }, random: true, speed: { min: 2, max: 6 }, straight: false }, number: { density: { enable: true, area: 800 }, value: 400 }, opacity: { value: { min: 0.1, max: 1 }, animation: { enable: true, speed: 4, minimumValue: 0 } }, shape: { type: "circle" }, size: { value: { min: 0.5, max: 2.5 }, animation: { enable: true, speed: 3, minimumValue: 0.1 } } }, detectRetina: true }} style={{ position: "absolute", top: 0, left: 0, width: "100%", height: "100%", zIndex: 0 }} />

      <motion.div className="login-card" initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.6, ease: "easeOut" }}>
        <div className="login-header"><div className="logo-3d">🤖</div><h2>AI <span>CRM</span></h2></div>
        <AnimatePresence mode="wait">
          {view === "login" && (
            <motion.form key="login" variants={formVariants} initial="hidden" animate="visible" exit="exit" className="login-form" onSubmit={handleLogin}>
              <div className="input-group"><User size={18} /><input type="text" placeholder="Email or Username" value={username} onChange={e => setUsername(e.target.value)} required /></div>
              <div className="input-group"><Lock size={18} /><input type={showPassword ? "text" : "password"} placeholder="Password" value={password} onChange={e => setPassword(e.target.value)} required /><button type="button" className="eye-btn" onClick={() => setShowPassword(!showPassword)}>{showPassword ? <EyeOff size={16} /> : <Eye size={16} />}</button></div>
              <div className="form-options"><button type="button" className="forgot-password" onClick={() => setView("forgot")}>Forgot Password?</button></div>
              <button type="submit" className="btn-login">Login Securely</button>
              <div className="signup-wrapper">Don't have an account? <button type="button" className="signup-link" onClick={() => setView("signup")}>Sign Up</button></div>
            </motion.form>
          )}
          {view === "signup" && (
            <motion.form key="signup" variants={formVariants} initial="hidden" animate="visible" exit="exit" className="login-form" onSubmit={handleSignup}>
              <div className="input-group"><User size={18} /><input type="text" placeholder="Choose a Username" value={username} onChange={e => setUsername(e.target.value)} required /></div>
              <div className="input-group"><Mail size={18} /><input type="email" placeholder="Email Address" value={email} onChange={e => setEmail(e.target.value)} required /></div>
              <div className="input-group"><Lock size={18} /><input type={showSignupPassword ? "text" : "password"} placeholder="Create Password" value={password} onChange={e => setPassword(e.target.value)} required /><button type="button" className="eye-btn" onClick={() => setShowSignupPassword(!showSignupPassword)}>{showSignupPassword ? <EyeOff size={16} /> : <Eye size={16} />}</button></div>
              <div className="input-group"><Lock size={18} /><input type={showConfirmPassword ? "text" : "password"} placeholder="Confirm Password" value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} required /><button type="button" className="eye-btn" onClick={() => setShowConfirmPassword(!showConfirmPassword)}>{showConfirmPassword ? <EyeOff size={16} /> : <Eye size={16} />}</button></div>
              <button type="submit" className="btn-login">Create Account</button>
              <button type="button" className="forgot-password" onClick={() => setView("login")} style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px', marginTop: '10px', width: '100%' }}><ArrowLeft size={14} /> Back to Login</button>
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
  
  // --- CHATGPT STYLE SESSION LOGIC ---
  const [currentSessionId, setCurrentSessionId] = useState(Date.now()); 
  
  const [showModal, setShowModal] = useState(false);
  const [isListening, setIsListening] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [itemToDelete, setItemToDelete] = useState(null);

  const [isTourActive, setIsTourActive] = useState(localStorage.getItem("isNewUser") === "true");
  const [tourStep, setTourStep] = useState(0);

  const tourData = [
    { targetId: "step1-newchat", title: "Start Fresh", desc: "Click this (+) button anytime to clear the screen and start a brand new conversation.", style: (isMob) => ({ top: '150px', left: isMob ? '10%' : '300px' }) },
    { targetId: "step2-input", title: "Command Center", desc: "Type commands here (like 'Assign lead') or use the Mic icon for fast voice inputs.", style: (isMob) => ({ bottom: '130px', left: isMob ? '5%' : '50%', transform: isMob ? 'none' : 'translateX(-50%)' }) },
    { targetId: "step3-utils", title: "System Controls", desc: "Use these utilities to fetch leads database, check status, or securely wipe chat memory.", style: (isMob) => ({ bottom: '60px', left: isMob ? '10%' : '300px' }) }
  ];

  const chatBoxRef = useRef(null);
  const isAdmin = userRole === "admin";
  const isSalesman = userRole === "salesman";
  const isCustomer = userRole === "customer";

  const triggerImageToast = (message) => {
    toast.custom((t) => (
      <div className="premium-toast">
        <div className="pt-progress"></div>
        <div className="pt-accent"></div>
        <div className="pt-icon-box"><div className="pt-icon-circle"><Check size={14} strokeWidth={4} /></div></div>
        <div className="pt-content"><h4>Success</h4><p>{message}</p></div>
        <button className="pt-close" onClick={() => toast.dismiss(t)}><X size={16} /></button>
      </div>
    ), { duration: 4000 });
  };

  const showWelcomeMessage = () => {
      let welcomeText = `👋 <b>Welcome to AI CRM Pro!</b><br><br>`;
      if (isAdmin) welcomeText += "👑 <b>ADMINISTRATOR ACCESS</b><br>Full database control enabled.<br><br>📝 <b>Assign Lead</b> → assign lead Ravi to Salesman_1<br>📋 <b>View DB</b> → show all leads / show all users<br>🗑 <b>Delete</b> → delete lead Ravi<br>⚙️ <b>System</b> → generate business report<br>";
      else if (isSalesman) welcomeText += "💼 <b>SALESMAN WORKSPACE</b><br>Manage your pipeline & follow-ups.<br><br>📋 <b>My Pipeline</b> → show my leads<br>🏆 <b>Update Status</b> → update Ravi status to Won<br>📅 <b>Add Note</b> → add follow-up for Ravi tomorrow<br>";
      else welcomeText += "👤 <b>CUSTOMER PORTAL</b><br>How can we assist you today?<br><br>➕ <b>New Request</b> → submit new requirement for Elevator<br>📋 <b>Check Status</b> → show my request status<br>📞 <b>Update Info</b> → update my phone to 9999999999<br>";
      welcomeText += "<br>━━━━━━━━━━━━━━━━━━━━<br>Type your command or use 🎤 voice!";
      setMessages([{ id: Date.now(), text: welcomeText, type: "bot" }]);
  };

  useEffect(() => {
    const timer = setTimeout(() => { setMessages(prev => { if (prev.length > 0) return prev; showWelcomeMessage(); return prev; }); }, 500);
    return () => clearTimeout(timer);
  }, [isAdmin, isSalesman, isCustomer]);

  useEffect(() => { if (chatBoxRef.current) chatBoxRef.current.scrollTop = chatBoxRef.current.scrollHeight; }, [messages]);

  useEffect(() => {
    if (isTourActive && window.innerWidth <= 768) {
      if (tourStep === 0 || tourStep === 2) setIsMobileMenuOpen(true);
      else setIsMobileMenuOpen(false);
    }
  }, [isTourActive, tourStep]);

  const addMessage = (text, type) => setMessages(prev => [...prev, { id: Date.now() + Math.random(), text, type }]);
  const addBotMessage = (text) => addMessage(text.replace(/\n/g, "<br>"), "bot");

  // --- FIXED: SAVES ONLY THE FIRST PROMPT AS HISTORY TITLE ---
  const saveToHistory = (userMsg) => {
    const sessionExists = chatSessions.some(session => session.id === currentSessionId);
    
    if (!sessionExists) {
        const titleText = userMsg.length > 25 ? userMsg.substring(0, 25) + "..." : userMsg;
        const newSession = { id: currentSessionId, title: titleText, time: new Date().toLocaleTimeString() };
        const updatedSessions = [newSession, ...chatSessions].slice(0, 15);
        setChatSessions(updatedSessions);
        localStorage.setItem("chatSessions", JSON.stringify(updatedSessions));
    }
  };

  // --- FIXED: CREATES NEW SESSION ID WHEN CLICKED ---
  const handleNewChat = () => {
      setCurrentSessionId(Date.now()); // Creates fresh session
      showWelcomeMessage(); 
      setInputValue(""); 
      triggerImageToast('Started a fresh conversation');
      if (window.innerWidth <= 768) { setIsMobileMenuOpen(false); }
  };

  const loadPastChat = (session) => {
      setCurrentSessionId(session.id); // Re-activates past session
      triggerImageToast(`Loaded: ${session.title}`);
      
      // Shows a visual cue that past chat context is loaded
      setMessages([{ 
          id: Date.now(), 
          text: `📂 <b>Chat Loaded</b><br>Session: ${session.title}<br><br><i>Note: Continue typing to add commands to this session history.</i>`, 
          type: "bot" 
      }]);
      
      if (window.innerWidth <= 768) setIsMobileMenuOpen(false);
  };

  const triggerDeleteHistoryItem = (id, e) => { e.stopPropagation(); setItemToDelete(id); };

  const confirmDeleteHistoryItem = () => {
    const updatedSessions = chatSessions.filter(session => session.id !== itemToDelete);
    setChatSessions(updatedSessions); 
    localStorage.setItem("chatSessions", JSON.stringify(updatedSessions));
    triggerImageToast('History removed successfully'); 
    setItemToDelete(null); 
    if (currentSessionId === itemToDelete) { handleNewChat(); }
  };

  const handleTourNext = () => {
      if (tourStep < tourData.length - 1) setTourStep(prev => prev + 1);
      else finishTour();
  };
  
  const finishTour = () => {
      setIsTourActive(false); localStorage.removeItem("isNewUser"); setIsMobileMenuOpen(false);
      triggerImageToast("You're all set! Let's get started.");
  };

  const handleSendMessage = async () => {
    const msg = inputValue.trim();
    if (msg === "") return;
    addMessage(msg, "user"); 
    saveToHistory(msg); // Will only save if this session ID is new!
    setInputValue("");

    const lowerMsg = msg.toLowerCase();
    let pendingAction = null;
    if (lowerMsg.includes("assign")) pendingAction = "assign";
    else if (lowerMsg.includes("delete") || lowerMsg.includes("remove")) pendingAction = "delete";
    else if (lowerMsg.includes("won") || lowerMsg.includes("completed")) pendingAction = "complete";

    const thinkingId = Date.now() + Math.random();
    setMessages(prev => [...prev, { id: thinkingId, text: "⏳ Processing securely...", type: "bot" }]);
    
    try {
      const res = await api.post("/api/chat/message", { message: msg, userId: "1", sessionId: currentSessionId, role: userRole.toUpperCase() });
      setMessages(prev => prev.filter(m => m.id !== thinkingId)); 
      addBotMessage(res.data.reply);
      if (pendingAction === "assign") triggerImageToast("Lead successfully assigned to pipeline!");
      else if (pendingAction === "delete") triggerImageToast("Lead data cleared from the system!");
      else if (pendingAction === "complete") triggerImageToast("Deal successfully closed! Great job!");
    } catch (error) {
      setMessages(prev => prev.filter(m => m.id !== thinkingId)); 
      addBotMessage("❌ Server not responding. Check connection.");
    }
  };

  const fetchDataForRole = () => {
    addBotMessage("🔄 Fetching authorized records..."); setIsMobileMenuOpen(false);
    triggerImageToast("Database records fetched successfully!"); 
    setTimeout(() => {
        let html = `📋 <b>Records Found: 1</b><br><br>━━━━━━━━━━━━━━━━━━━━<br>🔢 <b>Record #1</b><br>👤 Name        : Demo User<br>📄 Requirement: Sample Data<br>🔄 Status      : <b>Pending</b><br><br>━━━━━━━━━━━━━━━━━━━━`;
        addBotMessage(html);
    }, 1000);
  };

  const startVoice = () => {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!SpeechRecognition) { toast.error("Use Chrome for voice!"); return; }
    navigator.mediaDevices.getUserMedia({ audio: true }).then(() => {
        const recognition = new SpeechRecognition(); recognition.lang = "en-US"; recognition.continuous = false; recognition.interimResults = false;
        toast.info('Listening...'); 
        recognition.onstart = () => setIsListening(true);
        recognition.onresult = (event) => { const transcript = event.results[0][0].transcript; setInputValue(transcript); setIsListening(false); setTimeout(() => handleSendMessage(), 500); };
        recognition.onerror = () => setIsListening(false); recognition.onend = () => setIsListening(false);
        recognition.start();
      }).catch(() => { toast.error("Mic permission denied!"); });
  };

  const handleLogout = () => { triggerImageToast('Successfully logged out'); setAuth(false); }

  return (
    <div className="layout">
      {isTourActive && <div className="tour-overlay"></div>}

      <div className="mobile-header">
        <h2>AI <span>CRM</span></h2>
        <div style={{ display: 'flex', gap: '15px', alignItems: 'center' }}>
            <button className="theme-mini-btn" onClick={() => setIsDarkMode(!isDarkMode)} style={{ border:'none', background:'transparent', color:'var(--text-main)' }}><SunMoon size={22} /></button>
            <button className="menu-toggle-btn" onClick={() => setIsMobileMenuOpen(true)}><Menu size={26} /></button>
        </div>
      </div>
      <div className={`mobile-overlay ${isMobileMenuOpen && !isTourActive ? 'show' : ''}`} onClick={() => setIsMobileMenuOpen(false)}></div>

      <div className={`sidebar ${isMobileMenuOpen ? 'mobile-open' : ''}`} style={{ zIndex: isTourActive ? 10000 : '' }}>
        <div className="sidebar-top">
          <div className={`logo-area ${isTourActive && tourStep === 2 ? 'tour-target-glow' : ''}`}>
            <div className="logo-3d">🤖</div><h2>AI <span>CRM</span></h2>
            <div style={{ display: window.innerWidth > 768 ? 'flex' : 'none', marginLeft: 'auto' }}>
                <button className="theme-mini-btn" onClick={() => setIsDarkMode(!isDarkMode)} style={{ margin: 0 }} title="Toggle Theme"><SunMoon size={16} /></button>
            </div>
            <button className="menu-toggle-btn" onClick={() => setIsMobileMenuOpen(false)} style={{ display: window.innerWidth <= 768 ? 'block' : 'none', marginLeft: 'auto' }}><X size={24} /></button>
          </div>

          <div style={{ marginBottom: '24px', background: 'rgba(255,140,0,0.1)', padding: '10px 14px', borderRadius: '12px', display: 'flex', alignItems: 'center', gap: '10px', color: 'var(--primary)', fontSize: '13px', fontWeight: 'bold', border: '1px solid rgba(255,140,0,0.2)' }}>
            {isAdmin && <ShieldAlert size={16} />} {isSalesman && <Briefcase size={16} />} {isCustomer && <HeadphonesIcon size={16} />} {userRole.toUpperCase()} PORTAL
          </div>
          
          <div className="history-section">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                <p className="section-label" style={{ marginBottom: 0 }}>ACTIVITY LOG</p>
                <button className={`new-chat-btn ${isTourActive && tourStep === 0 ? 'tour-target-glow' : ''}`} onClick={handleNewChat} title="Start New Chat">
                    <Plus size={15} strokeWidth={2.5} />
                </button>
            </div>

            <div id="history-list">
              {chatSessions.length === 0 ? (
                <div className="history-item"><div className="history-content">No recent activity</div></div>
              ) : chatSessions.map(session => (
                  <div key={session.id} className={`history-item ${currentSessionId === session.id ? 'active-history' : ''}`} onClick={() => loadPastChat(session)}>
                    <div className="history-content"><MessageSquare size={14} /><span>{session.title}</span></div>
                    <button className="delete-history-btn" onClick={(e) => triggerDeleteHistoryItem(session.id, e)} title="Delete item"><X size={14} strokeWidth={2.5} /></button>
                  </div>
              ))}
            </div>
          </div>
        </div>
        
        <div className={`sidebar-bottom ${isTourActive && tourStep === 2 ? 'tour-target-glow' : ''}`}>
          <button className="util-btn active"><MessageSquare size={16} /> Chat Assistant</button>
          {isAdmin && (
              <button className="util-btn" onClick={() => { setShowModal(true); setIsMobileMenuOpen(false); }}><Eraser size={16} /> Clear System Chat</button>
          )}
          {isSalesman && <button className="util-btn" onClick={fetchDataForRole}><FileText size={16} /> My Assigned Pipeline</button>}
          {isCustomer && <button className="util-btn" onClick={fetchDataForRole}><FileText size={16} /> Check My Status</button>}
          <button className="util-btn" onClick={handleLogout} style={{ marginTop: '10px', color: '#ff4757' }}><LogOut size={16} color="#ff4757" /> Logout</button>
        </div>
      </div>

      <div className="main" style={{ zIndex: isTourActive && tourStep === 1 ? 9991 : '' }}>
        <div id="chat-box" ref={chatBoxRef}>
          <AnimatePresence>
            {messages.map((msg) => (
              <motion.div key={msg.id} className={msg.type} initial={{ opacity: 0, y: 10, scale: 0.97 }} animate={{ opacity: 1, y: 0, scale: 1 }} exit={{ opacity: 0, scale: 0.9 }} transition={{ duration: 0.3, ease: [0.34, 1.56, 0.64, 1] }} dangerouslySetInnerHTML={{ __html: msg.text }} />
            ))}
          </AnimatePresence>
        </div>
        <div className="input-container">
          <div className={`input-area ${isTourActive && tourStep === 1 ? 'tour-target' : ''}`}>
            <input value={inputValue} onChange={(e) => setInputValue(e.target.value)} onKeyDown={(e) => e.key === 'Enter' && handleSendMessage()} placeholder="Enter command or query..." autoFocus autoComplete="off" />
            <div className="action-buttons" style={{ display: 'flex', gap: '6px' }}>
              <button className={`icon-btn ${isListening ? 'listening' : ''}`} onClick={startVoice} style={isListening ? { background: '#ff4757', color: '#fff' } : {}}><Mic size={18} /></button>
              <button className="send-btn" onClick={handleSendMessage}><Send size={16} style={{ marginLeft: '-2px' }} /></button>
            </div>
          </div>
        </div>
      </div>

      <AnimatePresence>
        {isTourActive && (
            <motion.div 
                className="tour-tooltip"
                style={tourData[tourStep].style(window.innerWidth <= 768)}
                initial={{ opacity: 0, y: 15, scale: 0.95 }} animate={{ opacity: 1, y: 0, scale: 1 }} exit={{ opacity: 0, scale: 0.9 }} key={`tour-${tourStep}`} transition={{ duration: 0.3 }}
            >
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '12px' }}>
                    <div style={{ background: 'rgba(255,140,0,0.1)', color: 'var(--primary)', padding: '8px', borderRadius: '10px', display: 'flex' }}>
                        {tourStep === 0 ? <Plus size={20} /> : tourStep === 1 ? <MessageSquare size={20} /> : <Rocket size={20} />}
                    </div>
                    <h3 style={{ fontSize: '16px', margin: 0, fontFamily: "'Syne', sans-serif", fontWeight: 700 }}>{tourData[tourStep].title}</h3>
                </div>
                <p style={{ fontSize: '13.5px', color: 'var(--text-dim)', marginBottom: '20px', lineHeight: '1.5' }}>{tourData[tourStep].desc}</p>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: 'bold', letterSpacing: '1px' }}>STEP {tourStep + 1} OF 3</span>
                    <div style={{ display: 'flex', gap: '10px' }}>
                        <button onClick={finishTour} style={{ background: 'transparent', border: 'none', color: 'var(--text-dim)', cursor: 'pointer', fontSize: '13px', fontWeight: 600 }}>Skip</button>
                        <button onClick={handleTourNext} style={{ background: 'var(--primary-grad)', border: 'none', color: 'white', padding: '8px 16px', borderRadius: '8px', cursor: 'pointer', fontSize: '13.5px', fontWeight: 'bold', boxShadow: '0 4px 10px rgba(255,140,0,0.3)' }}>
                            {tourStep === 2 ? 'Done' : 'Next'}
                        </button>
                    </div>
                </div>
            </motion.div>
        )}

        {showModal && isAdmin && (
          <motion.div className="modal-overlay" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} style={{ display: 'flex', zIndex: 9999 }}>
            <motion.div className="modal-card" initial={{ opacity: 0, scale: 0.88 }} animate={{ opacity: 1, scale: 1 }} exit={{ opacity: 0, scale: 0.88 }} transition={{ duration: 0.25, ease: [0.34, 1.56, 0.64, 1] }}>
              <Trash2 size={35} color="#ff4757" style={{ margin: '0 auto 15px', display: 'block' }} />
              <h3>Wipe Chat History?</h3>
              <div className="modal-actions">
                <button className="btn-cancel" onClick={() => setShowModal(false)}>Cancel</button>
                <button className="btn-confirm" onClick={() => { showWelcomeMessage(); setShowModal(false); triggerImageToast("System chat memory wiped clean!"); }}>Clear System Memory</button>
              </div>
            </motion.div>
          </motion.div>
        )}

        {itemToDelete && (
          <motion.div className="modal-overlay" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} style={{ display: 'flex', zIndex: 9999 }}>
            <motion.div className="modal-card" initial={{ opacity: 0, scale: 0.88 }} animate={{ opacity: 1, scale: 1 }} exit={{ opacity: 0, scale: 0.88 }} transition={{ duration: 0.25, ease: [0.34, 1.56, 0.64, 1] }}>
              <Trash2 size={35} color="#ff4757" style={{ margin: '0 auto 15px', display: 'block' }} />
              <h3>Delete this history?</h3>
              <p style={{ color: 'var(--text-dim)', fontSize: '13.5px', margin: '6px 0 0' }}>This action cannot be undone.</p>
              <div className="modal-actions">
                <button className="btn-cancel" onClick={() => setItemToDelete(null)}>Cancel</button>
                <button className="btn-confirm" onClick={confirmDeleteHistoryItem}>Delete</button>
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
  useEffect(() => { localStorage.setItem("auth", isAuthenticated); localStorage.setItem("role", userRole); }, [isAuthenticated, userRole]);

  return (
    <>
      <Toaster position="top-right" theme={isDarkMode ? "dark" : "light"} toastOptions={{ unstyled: true }} />
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={!isAuthenticated ? <Login setAuth={setIsAuthenticated} setUserRole={setUserRole} isDarkMode={isDarkMode} setIsDarkMode={setIsDarkMode} /> : <Navigate to="/dashboard" />} />
          <Route path="/dashboard" element={isAuthenticated ? <Dashboard setAuth={setIsAuthenticated} userRole={userRole} isDarkMode={isDarkMode} setIsDarkMode={setIsDarkMode} /> : <Navigate to="/login" />} />
          <Route path="*" element={<Navigate to={isAuthenticated ? "/dashboard" : "/login"} />} />
        </Routes>
      </BrowserRouter>
    </>
  );
}