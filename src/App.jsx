import React, { useState, useEffect, useRef, useCallback } from 'react';
import { BrowserRouter, Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  SunMoon, MessageSquare, Users, Eraser, Mic, Send, Trash2, 
  Lock, User, LogOut, Mail, ArrowLeft, ShieldAlert, 
  Briefcase, HeadphonesIcon, FileText, Eye, EyeOff, Menu, X, Check, Plus,
  Rocket, Bot, Database // <--- PUTHU ICONS FOR TUTORIAL
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
        else { toast.error("Incorrect Admin Password!"); return; }
      } else if (lowerUser.includes("sales")) {
        role = "salesman";
      }

      setUserRole(role);
      setAuth(true);
      toast.success('Securely logged in to Workspace!');
      navigate("/dashboard");
    }
  };

  const handleSignup = (e) => {
    e.preventDefault();
    if (password !== confirmPassword) { toast.error("Passwords do not match!"); return; }
    
    // Set flag so dashboard knows it's a new user and shows tutorial
    localStorage.setItem("showTutorial", "true"); 
    
    toast.success(`Premium Account created for ${username}!`);
    setView("login");
  };

  const handleForgotPassword = (e) => {
    e.preventDefault();
    toast('Password reset link sent securely!', { icon: '🔐' });
    setView("login");
  };

  const formVariants = { hidden: { opacity: 0, x: -20 }, visible: { opacity: 1, x: 0, transition: { duration: 0.3 } }, exit: { opacity: 0, x: 20, transition: { duration: 0.2 } } };

  return (
    <div className="login-layout">
      <div style={{ position: 'absolute', top: 20, right: 20, zIndex: 9999 }}>
        <button 
          onClick={() => setIsDarkMode(!isDarkMode)}
          style={{ width: '40px', height: '40px', borderRadius: '50%', background: isDarkMode ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.04)', backdropFilter: 'blur(10px)', border: `1px solid ${isDarkMode ? 'rgba(255, 255, 255, 0.15)' : 'rgba(0,0,0,0.1)'}`, color: 'var(--primary)', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', boxShadow: '0 4px 15px rgba(0,0,0,0.1)', transition: 'all 0.3s ease' }}
          onMouseOver={(e) => { e.currentTarget.style.background = isDarkMode ? 'rgba(255, 255, 255, 0.15)' : 'rgba(0, 0, 0, 0.08)'; e.currentTarget.style.transform = 'scale(1.05)'; }}
          onMouseOut={(e) => { e.currentTarget.style.background = isDarkMode ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.04)'; e.currentTarget.style.transform = 'scale(1)'; }}
        >
          <SunMoon size={18} />
        </button>
      </div>

      <Particles id="tsparticles" init={particlesInit} options={{ fullScreen: { enable: false, zIndex: 0 }, background: { color: { value: "transparent" } }, fpsLimit: 120, interactivity: { events: { onHover: { enable: true, mode: "repulse" }, resize: true }, modes: { repulse: { distance: 100, duration: 0.4, speed: 1 } } }, particles: { color: { value: ["#ff8c00", "#ffcc00", "#ffffff", "#ff4500"] }, links: { enable: false }, move: { direction: "top", enable: true, outModes: { default: "out" }, random: true, speed: { min: 2, max: 6 }, straight: false }, number: { density: { enable: true, area: 800 }, value: 400 }, opacity: { value: { min: 0.1, max: 1 }, animation: { enable: true, speed: 4, minimumValue: 0 } }, shape: { type: "circle" }, size: { value: { min: 0.5, max: 2.5 }, animation: { enable: true, speed: 3, minimumValue: 0.1 } } }, detectRetina: true }} style={{ position: "absolute", top: 0, left: 0, width: "100%", height: "100%", zIndex: 0 }} />

      <motion.div className="login-card" initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.6, ease: "easeOut" }}>
        <div className="login-header">
          <div className="logo-3d">🤖</div><h2>AI <span>CRM</span></h2><p>{view === "login" && "Sign in to your account"}{view === "signup" && "Create your premium account"}{view === "forgot" && "Recover your account access"}</p>
        </div>
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
          {view === "forgot" && (
             <motion.form key="forgot" variants={formVariants} initial="hidden" animate="visible" exit="exit" className="login-form" onSubmit={handleForgotPassword}>
               <div className="input-group"><Mail size={18} /><input type="email" placeholder="Enter your registered email" value={email} onChange={e => setEmail(e.target.value)} required /></div>
               <button type="submit" className="btn-login">Send Reset Link</button>
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
  const [showModal, setShowModal] = useState(false);
  const [isListening, setIsListening] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [itemToDelete, setItemToDelete] = useState(null);
  
  // --- TUTORIAL STATES ---
  const [showTutorial, setShowTutorial] = useState(localStorage.getItem("showTutorial") === "true");
  const [tutorialStep, setTutorialStep] = useState(0);

  const tutorialContent = [
      { icon: <Rocket size={34} strokeWidth={2.5} />, title: "Welcome to AI CRM Pro", desc: "Your intelligent workspace is ready! Let's take a quick 3-step tour to boost your productivity and workflow." },
      { icon: <Bot size={34} strokeWidth={2.5} />, title: "Smart AI Assistant", desc: "Type commands or use voice input to assign leads, update deal statuses, and manage your pipeline instantly." },
      { icon: <Database size={34} strokeWidth={2.5} />, title: "Full System Control", desc: "Access the entire leads database, review activity logs, and securely wipe chat memory all in one place." }
  ];

  const chatBoxRef = useRef(null);
  const isAdmin = userRole === "admin";
  const isSalesman = userRole === "salesman";
  const isCustomer = userRole === "customer";

  const triggerImageToast = (message) => {
    toast.custom((t) => (
      <div className="premium-toast">
        <div className="pt-accent"></div>
        <div className="pt-icon-box"><div className="pt-icon-circle"><Check size={14} strokeWidth={4} /></div></div>
        <div className="pt-content"><h4>Success</h4><p>🔥 {message}</p></div>
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
    const timer = setTimeout(() => {
      setMessages(prev => {
        if (prev.length > 0) return prev; 
        showWelcomeMessage(); 
        return prev;
      });
    }, 500);
    return () => clearTimeout(timer);
  }, [isAdmin, isSalesman, isCustomer]);

  useEffect(() => { if (chatBoxRef.current) chatBoxRef.current.scrollTop = chatBoxRef.current.scrollHeight; }, [messages]);

  const addMessage = (text, type) => setMessages(prev => [...prev, { id: Date.now() + Math.random(), text, type }]);
  const addBotMessage = (text) => addMessage(text.replace(/\n/g, "<br>"), "bot");

  const saveToHistory = (userMsg) => {
    const session = { id: Date.now(), title: userMsg.substring(0, 30) + "...", time: new Date().toLocaleTimeString() };
    const updatedSessions = [session, ...chatSessions].slice(0, 10);
    setChatSessions(updatedSessions);
    localStorage.setItem("chatSessions", JSON.stringify(updatedSessions));
  };

  const handleNewChat = () => {
      showWelcomeMessage();
      setInputValue("");
      toast('Started a fresh conversation', { icon: '✨' });
      if (window.innerWidth <= 768) { setIsMobileMenuOpen(false); }
  };

  const triggerDeleteHistoryItem = (id, e) => {
    e.stopPropagation(); setItemToDelete(id);
  };

  const confirmDeleteHistoryItem = () => {
    const updatedSessions = chatSessions.filter(session => session.id !== itemToDelete);
    setChatSessions(updatedSessions);
    localStorage.setItem("chatSessions", JSON.stringify(updatedSessions));
    toast('History removed successfully', { icon: '🗑️' });
    setItemToDelete(null);
    showWelcomeMessage(); 
  };

  // --- TUTORIAL HANDLERS ---
  const handleTutorialNext = () => {
      if (tutorialStep < tutorialContent.length - 1) {
          setTutorialStep(prev => prev + 1);
      } else {
          finishTutorial();
      }
  };
  
  const finishTutorial = () => {
      setShowTutorial(false);
      localStorage.removeItem("showTutorial");
      toast.success("You're all set! Let's get started.", { icon: '🚀' });
  };

  const handleSendMessage = async () => {
    const msg = inputValue.trim();
    if (msg === "") return;
    addMessage(msg, "user"); saveToHistory(msg); setInputValue("");

    const lowerMsg = msg.toLowerCase();
    let pendingAction = null;
    if (lowerMsg.includes("assign")) pendingAction = "assign";
    else if (lowerMsg.includes("delete") || lowerMsg.includes("remove")) pendingAction = "delete";
    else if (lowerMsg.includes("won") || lowerMsg.includes("completed")) pendingAction = "complete";

    const thinkingId = Date.now() + Math.random();
    setMessages(prev => [...prev, { id: thinkingId, text: "⏳ Processing securely...", type: "bot" }]);
    
    try {
      const res = await api.post("/chat/message", { message: msg, userId: "1", sessionId: "session1", role: userRole });
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

  const fetchDataForRole = async () => {
    addBotMessage("🔄 Fetching authorized records..."); setIsMobileMenuOpen(false);
    try {
      let endpoint = "/leads/my"; if (isAdmin) endpoint = "/leads/all";
      const res = await api.get(`${endpoint}?role=${userRole}`);
      const leads = res.data;
      triggerImageToast("Database records fetched successfully!"); 
      if (!leads || leads.length === 0) { addBotMessage("📭 No records found in your access level."); return; }
      let html = `📋 <b>Records Found: ${leads.length}</b><br><br>`;
      leads.forEach((lead, index) => {
        html += "━━━━━━━━━━━━━━━━━━━━<br>"; html += `🔢 <b>Record #${index + 1}</b><br>`; html += `👤 Name        : ${lead.name || "-"}<br>`;
        if (isAdmin || isSalesman) { html += `📞 Phone       : ${lead.phone || "-"}<br>`; html += `📧 Email       : ${lead.email || "-"}<br>`; html += `🏙 City        : ${lead.city || "-"}<br>`; }
        html += `📄 Requirement: ${lead.requirement || "-"}<br>`; html += `🔄 Status      : <b>${lead.status || "Pending"}</b><br>`;
        if (isAdmin || isSalesman) { html += `💼 Deal Status : ${lead.dealStatus || "Open"}<br>`; html += `📅 Follow Up  : ${lead.followUp || "None"}<br>`; }
        if (isAdmin) { html += `📌 Source      : ${lead.source || "-"}<br>`; html += `👔 Assigned To : ${lead.assignedTo || "Unassigned"}<br>`; }
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
        const recognition = new SpeechRecognition(); recognition.lang = "en-US"; recognition.continuous = false; recognition.interimResults = false;
        toast('Listening...', { icon: '🎙️' }); 
        recognition.onstart = () => setIsListening(true);
        recognition.onresult = (event) => { const transcript = event.results[0][0].transcript; setInputValue(transcript); setIsListening(false); setTimeout(() => handleSendMessage(), 500); };
        recognition.onerror = () => setIsListening(false); recognition.onend = () => setIsListening(false);
        recognition.start();
      }).catch(() => { addBotMessage("❌ Mic permission denied!"); });
  };

  const handleLogout = () => { toast('Successfully logged out', { icon: '👋' }); setAuth(false); }

  return (
    <div className="layout">
      <div className="mobile-header">
        <h2>AI <span>CRM</span></h2>
        <div style={{ display: 'flex', gap: '15px', alignItems: 'center' }}>
            <button className="theme-mini-btn" onClick={() => setIsDarkMode(!isDarkMode)} style={{ border:'none', background:'transparent', color:'var(--text-main)' }}><SunMoon size={22} /></button>
            <button className="menu-toggle-btn" onClick={() => setIsMobileMenuOpen(true)}><Menu size={26} /></button>
        </div>
      </div>
      <div className={`mobile-overlay ${isMobileMenuOpen ? 'show' : ''}`} onClick={() => setIsMobileMenuOpen(false)}></div>

      <div className={`sidebar ${isMobileMenuOpen ? 'mobile-open' : ''}`}>
        <div className="sidebar-top">
          <div className="logo-area">
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
                <button className="new-chat-btn" onClick={handleNewChat} title="Start New Chat"><Plus size={12} strokeWidth={3} /> NEW</button>
            </div>

            <div id="history-list">
              {chatSessions.length === 0 ? (
                <div className="history-item"><div className="history-content">No recent activity</div></div>
              ) : chatSessions.map(session => (
                  <div key={session.id} className="history-item" onClick={() => toast('Loaded ' + session.title, { icon: '📂' })}>
                    <div className="history-content"><MessageSquare size={14} /><span>{session.title}</span></div>
                    <button className="delete-history-btn" onClick={(e) => triggerDeleteHistoryItem(session.id, e)} title="Delete item"><X size={14} strokeWidth={2.5} /></button>
                  </div>
              ))}
            </div>
          </div>
        </div>
        
        <div className="sidebar-bottom">
          <button className="util-btn active"><MessageSquare size={16} /> Chat Assistant</button>
          {isAdmin && (
            <>
              <button className="util-btn" onClick={fetchDataForRole}><Users size={16} /> All Leads DB</button>
              <button className="util-btn" onClick={() => { setShowModal(true); setIsMobileMenuOpen(false); }}><Eraser size={16} /> Clear System Chat</button>
            </>
          )}
          {isSalesman && <button className="util-btn" onClick={fetchDataForRole}><FileText size={16} /> My Assigned Pipeline</button>}
          {isCustomer && <button className="util-btn" onClick={fetchDataForRole}><FileText size={16} /> Check My Status</button>}
          <button className="util-btn" onClick={handleLogout} style={{ marginTop: '10px', color: '#ff4757' }}><LogOut size={16} color="#ff4757" /> Logout</button>
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
              <button className={`icon-btn ${isListening ? 'listening' : ''}`} onClick={startVoice} style={isListening ? { background: '#ff4757', color: '#fff' } : {}}><Mic size={18} /></button>
              <button className="send-btn" onClick={handleSendMessage}><Send size={16} style={{ marginLeft: '-2px' }} /></button>
            </div>
          </div>
        </div>
      </div>

      <AnimatePresence>
        {/* --- PREMIUM ONBOARDING TUTORIAL MODAL --- */}
        {showTutorial && (
          <motion.div className="modal-overlay" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} style={{ zIndex: 9999 }}>
            <motion.div className="tutorial-card" initial={{ scale: 0.9, y: 20 }} animate={{ scale: 1, y: 0 }} exit={{ scale: 0.9, opacity: 0 }} transition={{ duration: 0.4, ease: "easeOut" }}>
                
                <AnimatePresence mode="wait">
                  <motion.div 
                    key={tutorialStep}
                    initial={{ opacity: 0, x: 20 }} animate={{ opacity: 1, x: 0 }} exit={{ opacity: 0, x: -20 }} transition={{ duration: 0.3 }}
                  >
                    <div className="tutorial-icon">
                        {tutorialContent[tutorialStep].icon}
                    </div>
                    <h2>{tutorialContent[tutorialStep].title}</h2>
                    <p>{tutorialContent[tutorialStep].desc}</p>
                  </motion.div>
                </AnimatePresence>

                {/* Progress Dots */}
                <div className="tutorial-dots">
                    {tutorialContent.map((_, index) => (
                        <div key={index} className={`dot ${index === tutorialStep ? 'active' : ''}`}></div>
                    ))}
                </div>

                <div className="tutorial-actions">
                    <button className="btn-tut-skip" onClick={finishTutorial}>Skip Tour</button>
                    <button className="btn-tut-next" onClick={handleTutorialNext}>
                        {tutorialStep === tutorialContent.length - 1 ? "Get Started" : "Next Step"}
                    </button>
                </div>

            </motion.div>
          </motion.div>
        )}

        {showModal && isAdmin && (
          <motion.div className="modal-overlay" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} style={{ display: 'flex' }}>
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
          <motion.div className="modal-overlay" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} style={{ display: 'flex' }}>
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
      <Toaster position="top-right" theme={isDarkMode ? "dark" : "light"} toastOptions={{ style: { background: isDarkMode ? 'rgba(17, 21, 32, 0.85)' : 'rgba(255, 255, 255, 0.9)', color: isDarkMode ? '#e8edf5' : '#111520', backdropFilter: 'blur(12px)', border: '1px solid rgba(255, 140, 0, 0.5)', boxShadow: '0 8px 30px rgba(255, 140, 0, 0.15)', fontFamily: "'DM Sans', sans-serif", fontSize: '14px', fontWeight: '600' } }} />
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