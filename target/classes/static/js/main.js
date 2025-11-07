/* ==================== MAIN.JS - ENHANCED ==================== */

// ==================== SMOOTH SCROLL ====================
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        const target = document.querySelector(this.getAttribute('href'));
        if (target) {
            target.scrollIntoView({ 
                behavior: 'smooth', 
                block: 'start' 
            });
        }
    });
});

// ==================== ADD TO CART WITH ANIMATION ====================
document.querySelectorAll('.add-to-cart-form').forEach(form => {
    form.addEventListener('submit', function(e) {
        const btn = this.querySelector('.add-cart-btn');
        const originalText = btn.innerHTML;
        
        // Animation effect
// Animation effect
        btn.innerHTML = '<span style="display: inline-block; animation: spin 0.5s linear;">⏳</span> Đang thêm...';
        btn.disabled = true;
        btn.style.opacity = '0.7';
        
        // Animate cart icon
        const cartIcon = document.querySelector('.cart-link');
        if (cartIcon) {
            cartIcon.style.animation = 'pulse 0.5s ease';
            setTimeout(() => {
                cartIcon.style.animation = '';
            }, 500);
        }
    });
});

// ==================== PRODUCT CARD HOVER EFFECT ====================
document.querySelectorAll('.product-card').forEach(card => {
    card.addEventListener('mouseenter', function() {
        this.style.zIndex = '10';
        
        // Add ripple effect
        const ripple = document.createElement('div');
        ripple.style.cssText = `
            position: absolute;
            top: 50%;
            left: 50%;
            width: 0;
            height: 0;
            border-radius: 50%;
            background: radial-gradient(circle, rgba(255,107,107,0.3) 0%, transparent 70%);
            transform: translate(-50%, -50%);
            pointer-events: none;
            animation: rippleEffect 0.6s ease-out;
        `;
        this.appendChild(ripple);
        
        setTimeout(() => ripple.remove(), 600);
    });
    
    card.addEventListener('mouseleave', function() {
        this.style.zIndex = '1';
    });
});

// Add ripple animation
const rippleStyle = document.createElement('style');
rippleStyle.textContent = `
    @keyframes rippleEffect {
        to {
            width: 500px;
            height: 500px;
            opacity: 0;
        }
    }
`;
document.head.appendChild(rippleStyle);

// ==================== STICKY HEADER ON SCROLL ====================
let lastScroll = 0;
const header = document.querySelector('.header-main');
const headerTop = document.querySelector('.header-top');

window.addEventListener('scroll', () => {
    const currentScroll = window.pageYOffset;
    
    if (currentScroll > 100) {
        if (currentScroll > lastScroll) {
            // Scroll down - hide header top
            if (headerTop) {
                headerTop.style.transform = 'translateY(-100%)';
                headerTop.style.transition = 'transform 0.3s ease';
            }
        } else {
            // Scroll up - show header top
            if (headerTop) {
                headerTop.style.transform = 'translateY(0)';
            }
        }
    } else {
        if (headerTop) {
            headerTop.style.transform = 'translateY(0)';
        }
    }
    
    lastScroll = currentScroll;
});

// ==================== SEARCH BOX FOCUS EFFECT ====================
const searchInput = document.querySelector('.search-box input');
if (searchInput) {
    searchInput.addEventListener('focus', function() {
        this.parentElement.style.transform = 'scale(1.02)';
        this.parentElement.style.transition = 'transform 0.3s ease';
    });
    
    searchInput.addEventListener('blur', function() {
        this.parentElement.style.transform = 'scale(1)';
    });
    
    // Typing animation
    searchInput.addEventListener('input', function() {
        const btn = this.parentElement.querySelector('button');
        if (btn) {
            btn.style.animation = 'pulse 0.3s ease';
            setTimeout(() => {
                btn.style.animation = '';
            }, 300);
        }
    });
}

// ==================== LAZY LOADING IMAGES ====================
if ('IntersectionObserver' in window) {
    const imageObserver = new IntersectionObserver((entries, observer) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const img = entry.target;
                if (img.dataset.src) {
                    img.src = img.dataset.src;
                    img.classList.remove('lazy');
                    img.style.animation = 'fadeIn 0.5s ease';
                }
                imageObserver.unobserve(img);
            }
        });
    });
    
    document.querySelectorAll('img.lazy').forEach(img => {
        imageObserver.observe(img);
    });
}

// ==================== PRODUCT CARD ANIMATION ON SCROLL ====================
const observeProducts = new IntersectionObserver((entries) => {
    entries.forEach((entry, index) => {
        if (entry.isIntersecting) {
            entry.target.style.opacity = '0';
            entry.target.style.transform = 'translateY(30px)';
            
            setTimeout(() => {
                entry.target.style.transition = 'all 0.6s ease';
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }, index * 100);
            
            observeProducts.unobserve(entry.target);
        }
    });
}, { threshold: 0.1 });

document.querySelectorAll('.product-card').forEach(card => {
    observeProducts.observe(card);
});

// ==================== TOAST NOTIFICATION ====================
function showToast(message, type = 'success') {
    // Remove existing toasts
    document.querySelectorAll('.toast').forEach(t => {
        if (t.parentElement && t.parentElement.style.position === 'fixed') {
            t.remove();
        }
    });
    
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.style.cssText = `
        position: fixed;
        top: 100px;
        right: 30px;
        background: ${type === 'success' ? 'linear-gradient(135deg, #27ae60 0%, #2ecc71 100%)' : 'linear-gradient(135deg, #e74c3c 0%, #c0392b 100%)'};
        color: white;
        padding: 20px 30px;
        border-radius: 15px;
        box-shadow: 0 10px 40px ${type === 'success' ? 'rgba(39, 174, 96, 0.3)' : 'rgba(231, 76, 60, 0.3)'};
        z-index: 9999;
        animation: slideInRight 0.5s ease;
        display: flex;
        align-items: center;
        gap: 15px;
        font-weight: 600;
        max-width: 400px;
    `;
    
    const icon = document.createElement('span');
    icon.textContent = type === 'success' ? '✓' : '✕';
    icon.style.cssText = `
        font-size: 24px;
        background: rgba(255, 255, 255, 0.3);
        width: 40px;
        height: 40px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
    `;
    
    const text = document.createElement('span');
    text.textContent = message;
    
    toast.appendChild(icon);
    toast.appendChild(text);
    document.body.appendChild(toast);
    
    // Auto remove after 3 seconds
    setTimeout(() => {
        toast.style.animation = 'slideOutRight 0.5s ease';
        setTimeout(() => toast.remove(), 500);
    }, 3000);
}

// ==================== FORM VALIDATION ====================
const forms = document.querySelectorAll('form');
forms.forEach(form => {
    form.addEventListener('submit', function(e) {
        const inputs = this.querySelectorAll('input[required]');
        let isValid = true;
        
        inputs.forEach(input => {
            if (!input.value.trim()) {
                isValid = false;
                input.style.borderColor = '#e74c3c';
                input.style.animation = 'shake 0.5s ease';
                
                input.addEventListener('input', function() {
                    this.style.borderColor = '#e0e0e0';
                    this.style.animation = '';
                }, { once: true });
            }
        });
        
        if (!isValid) {
            e.preventDefault();
            showToast('Vui lòng điền đầy đủ thông tin!', 'error');
        }
    });
});

// ==================== BACK TO TOP BUTTON ====================
const backToTopBtn = document.createElement('button');
backToTopBtn.innerHTML = '↑';
backToTopBtn.className = 'back-to-top';
backToTopBtn.style.cssText = `
    position: fixed;
    bottom: 30px;
    right: 30px;
    background: linear-gradient(135deg, #e8342d 0%, #ff6b6b 100%);
    color: white;
    border: none;
    width: 50px;
    height: 50px;
    border-radius: 50%;
    font-size: 24px;
    font-weight: bold;
    cursor: pointer;
    display: none;
    z-index: 1000;
    box-shadow: 0 4px 15px rgba(232, 52, 45, 0.4);
    transition: all 0.3s ease;
`;

document.body.appendChild(backToTopBtn);

window.addEventListener('scroll', () => {
    if (window.pageYOffset > 300) {
        backToTopBtn.style.display = 'block';
        backToTopBtn.style.animation = 'scaleIn 0.3s ease';
    } else {
        backToTopBtn.style.display = 'none';
    }
});

backToTopBtn.addEventListener('click', () => {
    window.scrollTo({ 
        top: 0, 
        behavior: 'smooth' 
    });
});

backToTopBtn.addEventListener('mouseenter', function() {
    this.style.transform = 'scale(1.15) rotate(360deg)';
    this.style.boxShadow = '0 8px 25px rgba(232, 52, 45, 0.5)';
});

backToTopBtn.addEventListener('mouseleave', function() {
    this.style.transform = 'scale(1) rotate(0deg)';
    this.style.boxShadow = '0 4px 15px rgba(232, 52, 45, 0.4)';
});

// ==================== PRICE ANIMATION ====================
document.querySelectorAll('.product-price span:first-child').forEach(price => {
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.animation = 'pulse 1s ease';
                observer.unobserve(entry.target);
            }
        });
    });
    observer.observe(price);
});

// ==================== CART BADGE ANIMATION ====================
const cartBadge = document.querySelector('.cart-badge');
if (cartBadge) {
    const updateCartBadge = (count) => {
        cartBadge.textContent = count;
        cartBadge.style.animation = 'none';
        setTimeout(() => {
            cartBadge.style.animation = 'pulse 0.5s ease';
        }, 10);
    };
    
    // Observe cart badge changes
    const cartObserver = new MutationObserver((mutations) => {
        mutations.forEach((mutation) => {
            if (mutation.type === 'characterData' || mutation.type === 'childList') {
                cartBadge.style.animation = 'pulse 0.5s ease';
            }
        });
    });
    
    cartObserver.observe(cartBadge, {
        characterData: true,
        childList: true,
        subtree: true
    });
}

// ==================== SEARCH SUGGESTIONS (Optional Enhancement) ====================
const searchSuggestions = ['Gạo', 'Trứng', 'Sữa', 'Bánh mì', 'Dầu ăn', 'Mì gói', 'Coca Cola', 'Kem đánh răng'];

if (searchInput) {
    const suggestionsBox = document.createElement('div');
    suggestionsBox.style.cssText = `
        position: absolute;
        top: 100%;
        left: 0;
        right: 0;
        background: white;
        border-radius: 0 0 20px 20px;
        box-shadow: 0 10px 30px rgba(0,0,0,0.1);
        display: none;
        z-index: 1000;
        margin-top: 5px;
        overflow: hidden;
    `;
    
    searchInput.parentElement.style.position = 'relative';
    searchInput.parentElement.appendChild(suggestionsBox);
    
    searchInput.addEventListener('input', function() {
        const value = this.value.toLowerCase();
        if (value.length > 0) {
            const filtered = searchSuggestions.filter(s => 
                s.toLowerCase().includes(value)
            );
            
            if (filtered.length > 0) {
                suggestionsBox.innerHTML = filtered.map(s => `
                    <div style="padding: 12px 20px; cursor: pointer; transition: background 0.2s;" 
                         onmouseover="this.style.background='#fff5f5'" 
                         onmouseout="this.style.background='white'"
                         onclick="document.querySelector('.search-box input').value='${s}'; this.parentElement.style.display='none'">
                        🔍 ${s}
                    </div>
                `).join('');
                suggestionsBox.style.display = 'block';
            } else {
                suggestionsBox.style.display = 'none';
            }
        } else {
            suggestionsBox.style.display = 'none';
        }
    });
    
    // Close suggestions when clicking outside
    document.addEventListener('click', (e) => {
        if (!searchInput.parentElement.contains(e.target)) {
            suggestionsBox.style.display = 'none';
        }
    });
}

// ==================== LOADING ANIMATION ====================
window.addEventListener('load', () => {
    // Hide loading screen if exists
    const loader = document.querySelector('.loader');
    if (loader) {
        loader.style.animation = 'fadeOut 0.5s ease';
        setTimeout(() => loader.remove(), 500);
    }
    
    // Animate elements on page load
    document.querySelectorAll('.animate-on-load').forEach((el, index) => {
        el.style.opacity = '0';
        el.style.transform = 'translateY(20px)';
        setTimeout(() => {
            el.style.transition = 'all 0.6s ease';
            el.style.opacity = '1';
            el.style.transform = 'translateY(0)';
        }, index * 100);
    });
});

// ==================== ADD SHAKE ANIMATION ====================
const shakeStyle = document.createElement('style');
shakeStyle.textContent = `
    @keyframes shake {
        0%, 100% { transform: translateX(0); }
        25% { transform: translateX(-10px); }
        75% { transform: translateX(10px); }
    }
    
    @keyframes fadeOut {
        from { opacity: 1; }
        to { opacity: 0; }
    }
    
    @keyframes slideOutRight {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(400px);
            opacity: 0;
        }
    }
`;
document.head.appendChild(shakeStyle);

// ==================== CONSOLE WELCOME MESSAGE ====================
console.log('%c🏪 TapHoaPtit.online', 'font-size: 24px; font-weight: bold; color: #e8342d;');
console.log('%cChào mừng đến với tạp hóa trực tuyến số 1 Việt Nam!', 'font-size: 14px; color: #666;');
console.log('%c💻 Developed with ❤️', 'font-size: 12px; color: #27ae60;');

// ==================== DEBUG MODE (Development only) ====================
if (window.location.hostname === 'localhost') {
    console.log('%c🔧 Debug Mode Enabled', 'background: #ff6b6b; color: white; padding: 5px 10px; border-radius: 5px;');
    
    // Log all form submissions
    document.querySelectorAll('form').forEach(form => {
        form.addEventListener('submit', (e) => {
            console.log('Form submitted:', {
                action: form.action,
                method: form.method,
                data: new FormData(form)
            });
        });
    });
}

// ==================== PERFORMANCE OPTIMIZATION ====================
// Debounce function for scroll events
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Use debounced scroll handler
const debouncedScroll = debounce(() => {
    // Your scroll logic here
}, 100);

window.addEventListener('scroll', debouncedScroll);

console.log('✅ Main.js loaded successfully!');