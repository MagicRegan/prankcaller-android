const express = require('express');
const cors = require('cors');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const Database = require('better-sqlite3');
const { v4: uuidv4 } = require('uuid');
const { Vonage } = require('@vonage/server-sdk');
const path = require('path');
require('dotenv').config();

const app = express();
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Serve prank audio files
app.use('/audio', express.static(path.join(__dirname, 'audio')));

// ─── Config ──────────────────────────────────────────────────────────────────
const PORT = process.env.PORT || 3000;
const JWT_SECRET = process.env.JWT_SECRET || 'prankcaller-secret-key-change-in-production';
const VONAGE_API_KEY = process.env.VONAGE_API_KEY;
const VONAGE_API_SECRET = process.env.VONAGE_API_SECRET;
const VONAGE_FROM = process.env.VONAGE_PHONE_NUMBER || 'restricted';
const FREE_CREDITS = 5;
const BASE_URL = process.env.BASE_URL || `http://localhost:${PORT}`;

// ─── Vonage Client ───────────────────────────────────────────────────────────
let vonage = null;
if (VONAGE_API_KEY && VONAGE_API_SECRET) {
  vonage = new Vonage({
    apiKey: VONAGE_API_KEY,
    apiSecret: VONAGE_API_SECRET,
  });
}

// ─── Database Setup ──────────────────────────────────────────────────────────
const db = new Database(path.join(__dirname, 'prankcaller.db'));
db.pragma('journal_mode = WAL');

db.exec(`
  CREATE TABLE IF NOT EXISTS users (
    id TEXT PRIMARY KEY,
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    credits INTEGER DEFAULT ${FREE_CREDITS},
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
  );

  CREATE TABLE IF NOT EXISTS calls (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    prank_id INTEGER NOT NULL,
    prank_name TEXT,
    call_to TEXT NOT NULL,
    status TEXT DEFAULT 'pending',
    vonage_uuid TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
  );

  CREATE TABLE IF NOT EXISTS pranks (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    image_url TEXT,
    audio_file TEXT NOT NULL,
    category TEXT DEFAULT 'popular',
    calls_sent INTEGER DEFAULT 0,
    likes INTEGER DEFAULT 0
  );
`);

// ─── Seed pranks if empty ────────────────────────────────────────────────────
const prankCount = db.prepare('SELECT COUNT(*) as count FROM pranks').get();
if (prankCount.count === 0) {
  const seedPranks = [
    { id: 2, name: "You Got The Drugs?", desc: "Suspicious drug deal call", image: "2-large.png", audio: "prank_drugs.mp3", calls: 6000000, likes: 87400 },
    { id: 336, name: "Why You Call My Boyfriend", desc: "Jealous partner confrontation", image: "336-large.png", audio: "prank_boyfriend.mp3", calls: 3400000, likes: 56900 },
    { id: 349, name: "Snapchattin' My Girl", desc: "Social media drama prank", image: "349-large.png", audio: "prank_snapchat.mp3", calls: 3200000, likes: 37000 },
    { id: 3, name: "You Hit My Car", desc: "Angry car accident call", image: "3-large.png", audio: "prank_car.mp3", calls: 3400000, likes: 33900 },
    { id: 345, name: "Secret Crush", desc: "Secret admirer confession", image: "345-large.png", audio: "prank_crush.mp3", calls: 1900000, likes: 29600 },
    { id: 362, name: "Mexican Order", desc: "Confused restaurant order", image: "362-large.png", audio: "prank_mexican.mp3", calls: 2300000, likes: 27500 },
    { id: 355, name: "Show You The Way", desc: "Lost and confused directions", image: "355-large.png", audio: "prank_directions.mp3", calls: 934900, likes: 21900 },
    { id: 332, name: "STD Clinic", desc: "Awkward medical results call", image: "332-large.png", audio: "prank_clinic.mp3", calls: 2700000, likes: 21900 },
    { id: 356, name: "Found Your Number In A Stall", desc: "Bathroom wall number prank", image: "356-large.png", audio: "prank_stall.mp3", calls: 1600000, likes: 16800 },
    { id: 4, name: "Crazy Ex Girlfriend", desc: "Obsessive ex calling", image: "4-large.png", audio: "prank_ex.mp3", calls: 2100000, likes: 15200 },
    { id: 350, name: "Pizza Delivery Mix-up", desc: "Wrong pizza order chaos", image: "350-large.png", audio: "prank_pizza.mp3", calls: 1200000, likes: 14500 },
    { id: 360, name: "IRS Tax Audit", desc: "Fake IRS agent calling", image: "360-large.png", audio: "prank_irs.mp3", calls: 980000, likes: 12300 },
    { id: 361, name: "Wrong Number Romance", desc: "Accidental love confession", image: "361-large.png", audio: "prank_romance.mp3", calls: 870000, likes: 11800 },
    { id: 365, name: "Noise Complaint", desc: "Angry neighbor complaint", image: "365-large.png", audio: "prank_noise.mp3", calls: 750000, likes: 10200 },
    { id: 370, name: "Job Interview", desc: "Bizarre job interview call", image: "370-large.png", audio: "prank_interview.mp3", calls: 680000, likes: 9800 },
    { id: 375, name: "Parking Ticket", desc: "Outrageous parking fine", image: "375-large.png", audio: "prank_parking.mp3", calls: 620000, likes: 8900 },
    { id: 380, name: "Gym Membership", desc: "Aggressive gym sales call", image: "380-large.png", audio: "prank_gym.mp3", calls: 540000, likes: 7600 },
    { id: 385, name: "Lost Pet", desc: "Found your lost pet... maybe", image: "385-large.png", audio: "prank_pet.mp3", calls: 490000, likes: 7100 },
    { id: 390, name: "Tech Support Scam", desc: "Fake tech support call", image: "390-large.png", audio: "prank_tech.mp3", calls: 450000, likes: 6500 },
    { id: 395, name: "Lottery Winner", desc: "You've won the lottery!", image: "395-large.png", audio: "prank_lottery.mp3", calls: 410000, likes: 5900 },
  ];

  const insert = db.prepare(
    'INSERT INTO pranks (id, name, description, image_url, audio_file, calls_sent, likes) VALUES (?, ?, ?, ?, ?, ?, ?)'
  );
  for (const p of seedPranks) {
    insert.run(p.id, p.name, p.desc, p.image, p.audio, p.calls, p.likes);
  }
}

// ─── Auth Middleware ──────────────────────────────────────────────────────────
function authMiddleware(req, res, next) {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ status: 'error', message: 'Not authenticated' });
  }
  try {
    const token = authHeader.split(' ')[1];
    const decoded = jwt.verify(token, JWT_SECRET);
    req.userId = decoded.userId;
    req.userEmail = decoded.email;
    next();
  } catch (e) {
    return res.status(401).json({ status: 'error', message: 'Invalid or expired token' });
  }
}

// ─── Auth Routes ─────────────────────────────────────────────────────────────
app.post('/api/register', async (req, res) => {
  try {
    const { email, password } = req.body;
    if (!email || !password) {
      return res.status(400).json({ status: 'error', message: 'Email and password are required' });
    }
    if (password.length < 6) {
      return res.status(400).json({ status: 'error', message: 'Password must be at least 6 characters' });
    }

    const existing = db.prepare('SELECT id FROM users WHERE email = ?').get(email.toLowerCase());
    if (existing) {
      return res.status(400).json({ status: 'error', message: 'An account with this email already exists' });
    }

    const id = uuidv4();
    const passwordHash = await bcrypt.hash(password, 10);
    db.prepare('INSERT INTO users (id, email, password_hash) VALUES (?, ?, ?)').run(id, email.toLowerCase(), passwordHash);

    const token = jwt.sign({ userId: id, email: email.toLowerCase() }, JWT_SECRET, { expiresIn: '30d' });

    res.json({
      status: 'success',
      data: {
        token,
        userId: id,
        email: email.toLowerCase(),
        credits: FREE_CREDITS,
      }
    });
  } catch (e) {
    res.status(500).json({ status: 'error', message: 'Server error: ' + e.message });
  }
});

app.post('/api/login', async (req, res) => {
  try {
    const { email, password } = req.body;
    if (!email || !password) {
      return res.status(400).json({ status: 'error', message: 'Email and password are required' });
    }

    const user = db.prepare('SELECT * FROM users WHERE email = ?').get(email.toLowerCase());
    if (!user) {
      return res.status(401).json({ status: 'error', message: 'Invalid email or password' });
    }

    const valid = await bcrypt.compare(password, user.password_hash);
    if (!valid) {
      return res.status(401).json({ status: 'error', message: 'Invalid email or password' });
    }

    const token = jwt.sign({ userId: user.id, email: user.email }, JWT_SECRET, { expiresIn: '30d' });

    res.json({
      status: 'success',
      data: {
        token,
        userId: user.id,
        email: user.email,
        credits: user.credits,
      }
    });
  } catch (e) {
    res.status(500).json({ status: 'error', message: 'Server error: ' + e.message });
  }
});

// ─── User Routes ─────────────────────────────────────────────────────────────
app.get('/api/user', authMiddleware, (req, res) => {
  const user = db.prepare('SELECT id, email, credits, created_at FROM users WHERE id = ?').get(req.userId);
  if (!user) return res.status(404).json({ status: 'error', message: 'User not found' });
  res.json({ status: 'success', data: user });
});

app.post('/api/credits/add', authMiddleware, (req, res) => {
  const { amount } = req.body;
  const credits = parseInt(amount) || 0;
  if (credits <= 0) return res.status(400).json({ status: 'error', message: 'Invalid amount' });

  db.prepare('UPDATE users SET credits = credits + ? WHERE id = ?').run(credits, req.userId);
  const user = db.prepare('SELECT credits FROM users WHERE id = ?').get(req.userId);
  res.json({ status: 'success', data: { credits: user.credits } });
});

// ─── Pranks Routes ───────────────────────────────────────────────────────────
app.get('/api/pranks', (req, res) => {
  const pranks = db.prepare('SELECT * FROM pranks ORDER BY calls_sent DESC').all();
  const mapped = pranks.map(p => ({
    ...p,
    image_url: `https://pranks.prankcaller.io/images/${p.image_url}`,
    audio_url: `${BASE_URL}/audio/${p.audio_file}`,
  }));
  res.json({ status: 'success', data: mapped });
});

app.get('/api/pranks/:id', (req, res) => {
  const prank = db.prepare('SELECT * FROM pranks WHERE id = ?').get(req.params.id);
  if (!prank) return res.status(404).json({ status: 'error', message: 'Prank not found' });
  prank.image_url = `https://pranks.prankcaller.io/images/${prank.image_url}`;
  prank.audio_url = `${BASE_URL}/audio/${prank.audio_file}`;
  res.json({ status: 'success', data: prank });
});

// ─── Call Routes ─────────────────────────────────────────────────────────────
app.post('/api/call', authMiddleware, async (req, res) => {
  try {
    const { prankId, callTo, countryCode } = req.body;
    if (!prankId || !callTo) {
      return res.status(400).json({ status: 'error', message: 'prankId and callTo are required' });
    }

    // Check credits
    const user = db.prepare('SELECT credits FROM users WHERE id = ?').get(req.userId);
    if (!user || user.credits < 1) {
      return res.status(400).json({ status: 'error', message: 'Insufficient credits. Purchase more to continue.' });
    }

    // Get prank
    const prank = db.prepare('SELECT * FROM pranks WHERE id = ?').get(parseInt(prankId));
    if (!prank) {
      return res.status(400).json({ status: 'error', message: 'Prank not found' });
    }

    // Format phone number
    const cleanNumber = callTo.replace(/[^0-9+]/g, '');
    const fullNumber = (countryCode || '') + cleanNumber;

    // Create call record
    const callId = uuidv4();
    db.prepare('INSERT INTO calls (id, user_id, prank_id, prank_name, call_to, status) VALUES (?, ?, ?, ?, ?, ?)')
      .run(callId, req.userId, prank.id, prank.name, fullNumber, 'initiating');

    // Deduct credit
    db.prepare('UPDATE users SET credits = credits - 1 WHERE id = ?').run(req.userId);

    if (!vonage) {
      // No Vonage configured — simulate call
      db.prepare('UPDATE calls SET status = ? WHERE id = ?').run('simulated', callId);
      return res.json({
        status: 'success',
        data: {
          callId,
          message: 'Call simulated (Vonage not configured)',
          creditsRemaining: user.credits - 1,
        }
      });
    }

    // Make actual Vonage call
    const audioUrl = `${BASE_URL}/audio/${prank.audio_file}`;
    const ncco = [
      {
        action: 'stream',
        streamUrl: [audioUrl],
        bargeIn: false,
      }
    ];

    try {
      const response = await vonage.voice.createOutboundCall({
        to: [{ type: 'phone', number: fullNumber }],
        from: { type: 'phone', number: VONAGE_FROM },
        ncco: ncco,
      });

      db.prepare('UPDATE calls SET status = ?, vonage_uuid = ? WHERE id = ?')
        .run('ringing', response.uuid, callId);

      // Increment prank call count
      db.prepare('UPDATE pranks SET calls_sent = calls_sent + 1 WHERE id = ?').run(prank.id);

      res.json({
        status: 'success',
        data: {
          callId,
          vonageUuid: response.uuid,
          message: 'Call initiated successfully!',
          creditsRemaining: user.credits - 1,
        }
      });
    } catch (vonageErr) {
      // Refund credit on Vonage failure
      db.prepare('UPDATE users SET credits = credits + 1 WHERE id = ?').run(req.userId);
      db.prepare('UPDATE calls SET status = ? WHERE id = ?').run('failed', callId);
      res.status(500).json({
        status: 'error',
        message: 'Failed to place call: ' + (vonageErr.message || 'Unknown Vonage error'),
      });
    }
  } catch (e) {
    res.status(500).json({ status: 'error', message: 'Server error: ' + e.message });
  }
});

app.get('/api/calls', authMiddleware, (req, res) => {
  const calls = db.prepare('SELECT * FROM calls WHERE user_id = ? ORDER BY created_at DESC LIMIT 50').all(req.userId);
  res.json({ status: 'success', data: calls });
});

app.get('/api/call/:id/status', authMiddleware, async (req, res) => {
  const call = db.prepare('SELECT * FROM calls WHERE id = ? AND user_id = ?').get(req.params.id, req.userId);
  if (!call) return res.status(404).json({ status: 'error', message: 'Call not found' });

  if (call.vonage_uuid && vonage) {
    try {
      const details = await vonage.voice.getCall(call.vonage_uuid);
      const status = details.status || call.status;
      db.prepare('UPDATE calls SET status = ? WHERE id = ?').run(status, call.id);
      call.status = status;
    } catch (_) {
      // Use stored status
    }
  }

  res.json({ status: 'success', data: { callId: call.id, status: call.status } });
});

// ─── Vonage Webhook (call events) ────────────────────────────────────────────
app.post('/webhooks/events', (req, res) => {
  const { uuid, status } = req.body;
  if (uuid && status) {
    db.prepare('UPDATE calls SET status = ? WHERE vonage_uuid = ?').run(status, uuid);
  }
  res.status(200).send('OK');
});

app.post('/webhooks/answer', (req, res) => {
  // Fallback NCCO
  res.json([{ action: 'talk', text: 'This is a prank call. Goodbye!' }]);
});

// ─── Health Check ────────────────────────────────────────────────────────────
app.get('/health', (req, res) => {
  res.json({ status: 'ok', vonage: !!vonage });
});

// ─── Start ───────────────────────────────────────────────────────────────────
app.listen(PORT, '0.0.0.0', () => {
  console.log(`Prank Caller backend running on port ${PORT}`);
  console.log(`Vonage configured: ${!!vonage}`);
});
