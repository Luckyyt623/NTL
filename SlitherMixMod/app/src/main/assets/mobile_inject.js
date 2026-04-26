/**
 * SlitherMixMod - Mobile Injection Script
 * Bridges Chrome Extension API for NTL Mod + Adds Mobile Arrow/Joystick System
 */
(function() {
    'use strict';

    // ============================================================
    // CHROME EXTENSION API BRIDGE
    // Replace chrome.* calls used by NTL mod with WebView-compatible versions
    // ============================================================
    var ASSET_BASE = 'https://appassets.androidplatform.net/assets/';
    var MOD_VERSION = '9.18';
    var MOD_ID = 'slithermix_mobile_mod';

    window.chrome = {
        runtime: {
            id: MOD_ID,
            getURL: function(path) {
                return ASSET_BASE + path;
            },
            getManifest: function() {
                return { version: MOD_VERSION, name: 'SlitherMixMod' };
            },
            onMessage: {
                addListener: function(cb) {
                    window._ntl_msg_listeners = window._ntl_msg_listeners || [];
                    window._ntl_msg_listeners.push(cb);
                }
            },
            sendMessage: function(msg, cb) {
                if (msg && msg.greeting === 'oktab') {
                    window._ntl_oktab = true;
                }
            }
        },
        tabs: {
            query: function() {},
            sendMessage: function() {},
            update: function() {},
            create: function() {}
        },
        downloads: {
            setShelfEnabled: function() {}
        },
        permissions: {
            contains: function(req, cb) { cb && cb(true); }
        }
    };

    // Set localStorage values expected by NTL mod
    try {
        window.localStorage.setItem('loadbench', Date.now());
        window.localStorage.setItem('tinyscrID', MOD_ID);
        window.localStorage.setItem('myscrversion', MOD_VERSION);
    } catch(e) {}

    // ============================================================
    // LOAD NTL MOD ASSETS (replicate tinyscr.js behavior)
    // ============================================================
    function loadNTLMod() {
        // Clean up slither's native header/social elements
        try {
            ['logoih','twt','fb','csrvh'].forEach(function(id) {
                var el = document.getElementById(id);
                if (el) el.remove();
            });
        } catch(e) {}

        // Inject Bootstrap CSS
        var linkEl = document.createElement('LINK');
        linkEl.href = ASSET_BASE + 'bootstrap.css';
        linkEl.rel = 'stylesheet';
        linkEl.type = 'text/css';
        linkEl.media = 'screen';
        document.documentElement.appendChild(linkEl);

        // Load jQuery then main NTL mod script
        var jqScript = document.createElement('SCRIPT');
        jqScript.src = ASSET_BASE + 'jquery-2.2.4.min.js';
        jqScript.async = false;
        document.documentElement.appendChild(jqScript);

        jqScript.addEventListener('load', function() {
            var mainScript = document.createElement('SCRIPT');
            mainScript.src = ASSET_BASE + 'main-mt.js';
            mainScript.async = false;
            document.documentElement.appendChild(mainScript);
            mainScript.addEventListener('load', function() {
                console.log('[SlitherMix] NTL Mod loaded');
                injectMobileControls();
            });
        });
    }

    // ============================================================
    // ARROW / JOYSTICK SYSTEM (Mobile SWF-style controls)
    // ============================================================
    function injectMobileControls() {
        var style = document.createElement('style');
        style.textContent = `
            #smx-controls {
                position: fixed;
                bottom: 0; left: 0; right: 0;
                pointer-events: none;
                z-index: 9999999;
            }
            /* === JOYSTICK === */
            #smx-joystick-area {
                position: fixed;
                bottom: 24px; left: 24px;
                width: 160px; height: 160px;
                pointer-events: all;
                touch-action: none;
                user-select: none;
                -webkit-user-select: none;
            }
            #smx-joystick-base {
                position: absolute;
                inset: 0;
                border-radius: 50%;
                background: radial-gradient(circle, rgba(255,255,255,0.10) 0%, rgba(255,255,255,0.04) 70%);
                border: 2.5px solid rgba(255,255,255,0.30);
                box-shadow: 0 0 20px rgba(100,200,255,0.2);
            }
            /* Arrow indicators */
            .smx-arrow {
                position: absolute;
                width: 0; height: 0;
                opacity: 0.55;
            }
            .smx-arrow-up {
                border-left: 9px solid transparent;
                border-right: 9px solid transparent;
                border-bottom: 16px solid rgba(255,255,255,0.7);
                top: 8px; left: 50%; transform: translateX(-50%);
            }
            .smx-arrow-down {
                border-left: 9px solid transparent;
                border-right: 9px solid transparent;
                border-top: 16px solid rgba(255,255,255,0.7);
                bottom: 8px; left: 50%; transform: translateX(-50%);
            }
            .smx-arrow-left {
                border-top: 9px solid transparent;
                border-bottom: 9px solid transparent;
                border-right: 16px solid rgba(255,255,255,0.7);
                left: 8px; top: 50%; transform: translateY(-50%);
            }
            .smx-arrow-right {
                border-top: 9px solid transparent;
                border-bottom: 9px solid transparent;
                border-left: 16px solid rgba(255,255,255,0.7);
                right: 8px; top: 50%; transform: translateY(-50%);
            }
            #smx-joystick-knob {
                position: absolute;
                width: 56px; height: 56px;
                border-radius: 50%;
                background: radial-gradient(circle at 35% 35%, rgba(120,220,255,0.9), rgba(40,120,200,0.7));
                border: 2px solid rgba(255,255,255,0.5);
                box-shadow: 0 2px 12px rgba(0,0,0,0.5), inset 0 1px 3px rgba(255,255,255,0.3);
                left: 50%; top: 50%;
                transform: translate(-50%, -50%);
                transition: transform 0.05s ease-out;
                pointer-events: none;
            }
            /* === BOOST BUTTON === */
            #smx-boost-btn {
                position: fixed;
                bottom: 50px; right: 40px;
                width: 88px; height: 88px;
                border-radius: 50%;
                background: radial-gradient(circle at 35% 35%, rgba(255,80,80,0.9), rgba(180,20,20,0.8));
                border: 3px solid rgba(255,150,150,0.6);
                box-shadow: 0 0 20px rgba(255,60,60,0.4), inset 0 1px 4px rgba(255,255,255,0.2);
                display: flex; align-items: center; justify-content: center;
                font-size: 28px;
                color: white;
                text-shadow: 0 1px 3px rgba(0,0,0,0.5);
                pointer-events: all;
                touch-action: none;
                user-select: none;
                -webkit-user-select: none;
                cursor: pointer;
                transition: transform 0.1s, box-shadow 0.1s;
            }
            #smx-boost-btn:active, #smx-boost-btn.active {
                transform: scale(0.92);
                box-shadow: 0 0 30px rgba(255,60,60,0.7), inset 0 1px 4px rgba(255,255,255,0.2);
            }
            /* === DIRECTIONAL INDICATOR (arrow trail showing current direction) === */
            #smx-dir-indicator {
                position: absolute;
                width: 4px;
                background: linear-gradient(to top, transparent, rgba(100,220,255,0.8));
                border-radius: 2px;
                left: 50%; top: 50%;
                transform-origin: 0% 0%;
                transform: translateX(-50%) rotate(0deg);
                height: 60px;
                pointer-events: none;
                opacity: 0;
                transition: opacity 0.2s;
            }
        `;
        document.head.appendChild(style);

        // Build joystick DOM
        var joyArea = document.createElement('div');
        joyArea.id = 'smx-joystick-area';
        joyArea.innerHTML = `
            <div id="smx-joystick-base">
                <div class="smx-arrow smx-arrow-up"></div>
                <div class="smx-arrow smx-arrow-down"></div>
                <div class="smx-arrow smx-arrow-left"></div>
                <div class="smx-arrow smx-arrow-right"></div>
                <div id="smx-dir-indicator"></div>
            </div>
            <div id="smx-joystick-knob"></div>
        `;
        document.body.appendChild(joyArea);

        // Boost button
        var boostBtn = document.createElement('div');
        boostBtn.id = 'smx-boost-btn';
        boostBtn.textContent = '⚡';
        document.body.appendChild(boostBtn);

        // ---- Joystick Logic ----
        var knob = document.getElementById('smx-joystick-knob');
        var dirIndicator = document.getElementById('smx-dir-indicator');
        var joyRect, touchId = null;
        var joyX = 0, joyY = 0;
        var MAX_RADIUS = 55;
        var joystickActive = false;

        // Find the game canvas to send events to
        function getCanvas() {
            return document.getElementById('mc') || document.querySelector('canvas');
        }

        function getCanvasCenter() {
            var c = getCanvas();
            if (!c) return { x: window.innerWidth / 2, y: window.innerHeight / 2 };
            var r = c.getBoundingClientRect();
            return { x: r.left + r.width / 2, y: r.top + r.height / 2 };
        }

        function sendMouseMove(px, py) {
            var canvas = getCanvas();
            if (!canvas) return;
            var evt = new MouseEvent('mousemove', {
                bubbles: true, cancelable: true,
                clientX: px, clientY: py,
                view: window
            });
            canvas.dispatchEvent(evt);
            // Also try pointer event
            try {
                var pe = new PointerEvent('pointermove', {
                    bubbles: true, cancelable: true,
                    clientX: px, clientY: py,
                    view: window
                });
                canvas.dispatchEvent(pe);
            } catch(e) {}
        }

        function sendBoost(down) {
            var canvas = getCanvas();
            if (!canvas) return;
            var type = down ? 'mousedown' : 'mouseup';
            var evt = new MouseEvent(type, {
                bubbles: true, cancelable: true,
                clientX: window.innerWidth / 2,
                clientY: window.innerHeight / 2,
                button: 0, buttons: down ? 1 : 0,
                view: window
            });
            canvas.dispatchEvent(evt);
        }

        function updateJoystick(relX, relY) {
            var dist = Math.sqrt(relX*relX + relY*relY);
            var clampedX = relX, clampedY = relY;
            if (dist > MAX_RADIUS) {
                clampedX = (relX / dist) * MAX_RADIUS;
                clampedY = (relY / dist) * MAX_RADIUS;
            }

            // Move knob
            knob.style.transform = 'translate(calc(-50% + ' + clampedX + 'px), calc(-50% + ' + clampedY + 'px))';

            // Show direction indicator
            if (dist > 5) {
                var angle = Math.atan2(clampedY, clampedX) * 180 / Math.PI + 90;
                dirIndicator.style.opacity = '0.8';
                dirIndicator.style.transform = 'translateX(-50%) rotate(' + angle + 'deg)';
            } else {
                dirIndicator.style.opacity = '0';
            }

            // Translate joystick to canvas mouse coordinates
            var center = getCanvasCenter();
            // Scale: full joystick deflection = ~300px from center
            var SCALE = 4.5;
            var targetX = center.x + clampedX * SCALE;
            var targetY = center.y + clampedY * SCALE;
            sendMouseMove(targetX, targetY);

            joyX = clampedX; joyY = clampedY;
        }

        joyArea.addEventListener('touchstart', function(e) {
            e.preventDefault();
            if (touchId !== null) return;
            joyRect = joyArea.getBoundingClientRect();
            var touch = e.changedTouches[0];
            touchId = touch.identifier;
            joystickActive = true;
            var cx = joyRect.left + joyRect.width/2;
            var cy = joyRect.top + joyRect.height/2;
            updateJoystick(touch.clientX - cx, touch.clientY - cy);
        }, { passive: false });

        joyArea.addEventListener('touchmove', function(e) {
            e.preventDefault();
            for (var i = 0; i < e.changedTouches.length; i++) {
                if (e.changedTouches[i].identifier === touchId) {
                    var touch = e.changedTouches[i];
                    var cx = joyRect.left + joyRect.width/2;
                    var cy = joyRect.top + joyRect.height/2;
                    updateJoystick(touch.clientX - cx, touch.clientY - cy);
                    break;
                }
            }
        }, { passive: false });

        function releaseJoystick(e) {
            e.preventDefault();
            for (var i = 0; i < e.changedTouches.length; i++) {
                if (e.changedTouches[i].identifier === touchId) {
                    touchId = null;
                    joystickActive = false;
                    knob.style.transform = 'translate(-50%, -50%)';
                    dirIndicator.style.opacity = '0';
                    // Send mouse to center to stop movement
                    var center = getCanvasCenter();
                    sendMouseMove(center.x, center.y);
                    break;
                }
            }
        }
        joyArea.addEventListener('touchend', releaseJoystick, { passive: false });
        joyArea.addEventListener('touchcancel', releaseJoystick, { passive: false });

        // ---- Boost Button Logic ----
        boostBtn.addEventListener('touchstart', function(e) {
            e.preventDefault();
            boostBtn.classList.add('active');
            sendBoost(true);
        }, { passive: false });

        boostBtn.addEventListener('touchend', function(e) {
            e.preventDefault();
            boostBtn.classList.remove('active');
            sendBoost(false);
        }, { passive: false });

        boostBtn.addEventListener('touchcancel', function(e) {
            e.preventDefault();
            boostBtn.classList.remove('active');
            sendBoost(false);
        }, { passive: false });

        // ---- Hide controls when NTL mod UI panels are open ----
        var observer = new MutationObserver(function() {
            var ntlPanel = document.getElementById('ntl_panel') || document.getElementById('ntlpanel');
            var visible = ntlPanel && ntlPanel.style.display !== 'none';
            joyArea.style.display = visible ? 'none' : 'block';
            boostBtn.style.display = visible ? 'none' : 'flex';
        });
        observer.observe(document.body, { childList: true, subtree: true, attributes: true });

        console.log('[SlitherMix] Mobile controls injected');
    }

    // ============================================================
    // SKIN CUSTOMIZATION BRIDGE
    // Exposes API for Android to set custom skins
    // ============================================================
    window.SlitherMixSkin = {
        // Set snake colors (hue1: 0-360, hue2: 0-360)
        setColors: function(hue1, hue2) {
            try {
                localStorage.setItem('myCc', hue1.toString());
                localStorage.setItem('myCc2', hue2.toString());
                // Trigger NTL mod color update if available
                if (typeof window.myCc !== 'undefined') window.myCc = hue1;
                if (typeof window.myCc2 !== 'undefined') window.myCc2 = hue2;
                if (typeof window.ntl_apply_skin === 'function') window.ntl_apply_skin();
            } catch(e) {}
        },
        // Set skin pattern (0-59)
        setPattern: function(n) {
            try {
                localStorage.setItem('mysk', n.toString());
                if (typeof window.mysk !== 'undefined') window.mysk = n;
            } catch(e) {}
        },
        // Set accessory
        setAccessory: function(acc) {
            try {
                localStorage.setItem('myacc', acc);
                if (typeof window.myacc !== 'undefined') window.myacc = acc;
            } catch(e) {}
        },
        // Random skin
        randomSkin: function() {
            var h1 = Math.floor(Math.random() * 360);
            var h2 = Math.floor(Math.random() * 360);
            var sk = Math.floor(Math.random() * 60);
            this.setColors(h1, h2);
            this.setPattern(sk);
        },
        // Get current skin info
        getCurrent: function() {
            return {
                hue1: parseInt(localStorage.getItem('myCc') || '0'),
                hue2: parseInt(localStorage.getItem('myCc2') || '120'),
                pattern: parseInt(localStorage.getItem('mysk') || '0'),
                accessory: localStorage.getItem('myacc') || 'none'
            };
        }
    };

    // ============================================================
    // ANDROID BRIDGE (called from Java via evaluateJavascript)
    // ============================================================
    window.AndroidBridge = {
        onSkinSelected: function(hue1, hue2, pattern, accessory) {
            window.SlitherMixSkin.setColors(hue1, hue2);
            window.SlitherMixSkin.setPattern(pattern);
            window.SlitherMixSkin.setAccessory(accessory);
        },
        hideControls: function(hide) {
            var joy = document.getElementById('smx-joystick-area');
            var boost = document.getElementById('smx-boost-btn');
            if (joy) joy.style.display = hide ? 'none' : 'block';
            if (boost) boost.style.display = hide ? 'none' : 'flex';
        },
        getScore: function() {
            try {
                return typeof snake !== 'undefined' ? snake.sc || 0 : 0;
            } catch(e) { return 0; }
        }
    };

    // ============================================================
    // INIT - Start after DOM is ready
    // ============================================================
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', loadNTLMod);
    } else {
        // Wait a tick to ensure slither.io scripts have run
        setTimeout(loadNTLMod, 500);
    }

    // Also listen for page ready signal
    window.addEventListener('load', function() {
        if (!window._smx_loaded) {
            window._smx_loaded = true;
            // Already handled above, but catch any edge case
        }
    });

    console.log('[SlitherMixMod] Bridge initialized');
})();
