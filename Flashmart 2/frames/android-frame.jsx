// @ds-adherence-ignore -- device frame scaffold (raw elements/hex/px by design)

/* BEGIN USAGE */
// android-frame.jsx — Premium Pixel-style Material 3 device frame.
// Bezel + status bar (time left / icons right) + gesture-nav pill.
// Screens supply their own M3 chrome (top app bar, nav bar, FAB).
// Exports (to window): AndroidDevice, AndroidStatusBar, AndroidNavBar, AndroidKeyboard
//
//   <AndroidDevice>…screen…</AndroidDevice>
//   <AndroidDevice dark statusTint="var(--brand)">…</AndroidDevice>
/* END USAGE */

const AND_TOP = 40;   // status-bar clearance
const AND_BOTTOM = 24; // gesture-nav clearance

// ─────────────────────────────────────────────────────────────
// Status bar — Android: time on the left, signal/wifi/battery right
// ─────────────────────────────────────────────────────────────
function AndroidStatusBar({ dark = false, time = '9:30' }) {
  const c = dark ? '#fff' : '#181d18';
  return (
    <div style={{
      height: AND_TOP, display: 'flex', alignItems: 'center', justifyContent: 'space-between',
      padding: '0 22px 0 24px', position: 'relative', zIndex: 20,
      fontFamily: 'var(--roboto)',
    }}>
      <span style={{ fontSize: 14, fontWeight: 600, letterSpacing: '.01em', color: c }}>{time}</span>
      <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
        {/* signal */}
        <svg width="17" height="12" viewBox="0 0 17 12"><path d="M15.5 1v10M11.6 3.5v7.5M7.7 6v5M3.8 8v3" stroke={c} strokeWidth="2.2" strokeLinecap="round"/></svg>
        {/* wifi */}
        <svg width="16" height="12" viewBox="0 0 16 12"><path d="M8 11.2 1 4.4a10 10 0 0 1 14 0L8 11.2Z" fill={c}/></svg>
        {/* battery */}
        <svg width="22" height="12" viewBox="0 0 22 12"><rect x="1" y="1.5" width="18" height="9" rx="2.4" fill="none" stroke={c} strokeOpacity=".5" strokeWidth="1.2"/><rect x="2.6" y="3" width="13.5" height="6" rx="1.4" fill={c}/><rect x="20" y="4" width="1.6" height="4" rx=".8" fill={c} fillOpacity=".5"/></svg>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// Gesture nav pill
// ─────────────────────────────────────────────────────────────
function AndroidNavBar({ dark = false }) {
  return (
    <div style={{ height: AND_BOTTOM, display: 'flex', alignItems: 'center', justifyContent: 'center',
      position: 'relative', zIndex: 60, pointerEvents: 'none' }}>
      <div style={{ width: 128, height: 5, borderRadius: 3, background: dark ? 'rgba(255,255,255,.85)' : '#181d18', opacity: dark ? .85 : .85 }} />
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// Device frame
// ─────────────────────────────────────────────────────────────
function AndroidDevice({ children, width = 412, height = 892, dark = false, statusDark }) {
  const sd = statusDark != null ? statusDark : dark;
  return (
    <div style={{
      width, height, borderRadius: 46, position: 'relative', flexShrink: 0,
      background: 'linear-gradient(150deg, #2b2c30, #0c0d0f)',
      padding: 4, boxSizing: 'border-box',
      boxShadow: '0 40px 80px rgba(0,0,0,0.22), 0 0 0 1px rgba(0,0,0,0.16)',
      fontFamily: 'var(--roboto)', WebkitFontSmoothing: 'antialiased',
    }}>
      <div style={{
        width: '100%', height: '100%', borderRadius: 42, overflow: 'hidden', position: 'relative',
        background: dark ? '#101411' : 'var(--m3-surface)',
      }}>
        {/* punch-hole camera */}
        <div style={{ position: 'absolute', top: 13, left: '50%', transform: 'translateX(-50%)',
          width: 12, height: 12, borderRadius: '50%', background: '#000', zIndex: 50,
          boxShadow: '0 0 0 2px rgba(0,0,0,.25)' }} />
        {/* status bar */}
        <div style={{ position: 'absolute', top: 0, left: 0, right: 0, zIndex: 20 }}>
          <AndroidStatusBar dark={sd} />
        </div>
        {/* content */}
        <div style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
          <div style={{ flex: 1, overflow: 'hidden', position: 'relative' }}>{children}</div>
        </div>
        {/* gesture nav */}
        <div style={{ position: 'absolute', bottom: 0, left: 0, right: 0, zIndex: 60 }}>
          <AndroidNavBar dark={dark} />
        </div>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// Gboard keyboard (Material 3) — light surface
// ─────────────────────────────────────────────────────────────
function AndroidKeyboard({ dark = false }) {
  const surf = dark ? '#2a302c' : '#dde4df';
  const keyBg = dark ? '#3a423c' : '#fff';
  const ink = dark ? '#e2e6e2' : '#1b211d';
  let _k = 0;
  const key = (l, { flex = 1, bg = keyBg, r = 7, minW, fs = 20 } = {}) => (
    <div key={_k++} style={{ height: 44, borderRadius: r, flex, minWidth: minW, background: bg,
      display: 'flex', alignItems: 'center', justifyContent: 'center', fontFamily: 'var(--roboto)',
      fontSize: fs, color: ink, boxShadow: '0 1px 0 rgba(0,0,0,.08)' }}>{l}</div>
  );
  const row = (keys, style = {}) => (
    <div style={{ display: 'flex', gap: 6, justifyContent: 'center', ...style }}>{keys.map(l => key(l))}</div>
  );
  return (
    <div style={{ background: surf, padding: '8px 6px 10px', display: 'flex', flexDirection: 'column', gap: 8,
      borderRadius: '18px 18px 0 0' }}>
      {row(['q','w','e','r','t','y','u','i','o','p'])}
      {row(['a','s','d','f','g','h','j','k','l'], { padding: '0 18px' })}
      <div style={{ display: 'flex', gap: 6 }}>
        {key('⇧', { bg: 'transparent', minW: 40, fs: 18 })}
        <div style={{ display: 'flex', gap: 6, flex: 7 }}>{['z','x','c','v','b','n','m'].map(l => key(l))}</div>
        {key('⌫', { bg: 'transparent', minW: 40, fs: 18 })}
      </div>
      <div style={{ display: 'flex', gap: 6 }}>
        {key('?123', { minW: 56, fs: 13 })}
        {key(',', { minW: 36 })}
        {key('', { flex: 4 })}
        {key('.', { minW: 36 })}
        {key('→', { bg: 'var(--m3-primary)', minW: 56, r: 100, fs: 18 })}
      </div>
    </div>
  );
}

Object.assign(window, { AndroidDevice, AndroidStatusBar, AndroidNavBar, AndroidKeyboard, AND_TOP, AND_BOTTOM });
