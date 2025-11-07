// ==================== MODAL FUNCTIONS ====================
function openOrderModal() {
    const modal = document.getElementById('orderModal');
    if (modal) {
        modal.style.display = 'flex';
        document.body.style.overflow = 'hidden';
    }
}

function closeOrderModal() {
    const modal = document.getElementById('orderModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
    }
}

// Close modal when pressing ESC
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        closeOrderModal();
    }
});

// Form validation
const orderForm = document.getElementById('orderForm');
if (orderForm) {
    orderForm.addEventListener('submit', function(e) {
        const phone = document.getElementById('phone').value.trim();
        const address = document.getElementById('address').value.trim();
        
        if (!phone || !address) {
            e.preventDefault();
            showToast('Vui lòng điền đầy đủ thông tin!', 'error');
            return false;
        }
        
        // Validate phone number (Vietnamese format)
        const phoneRegex = /(84|0[3|5|7|8|9])+([0-9]{8})\b/;
        if (!phoneRegex.test(phone)) {
            e.preventDefault();
            showToast('Số điện thoại không hợp lệ!', 'error');
            return false;
        }
        
        // Show loading
        const submitBtn = this.querySelector('.btn-confirm');
        submitBtn.innerHTML = '<span class="loading"></span> Đang xử lý...';
        submitBtn.disabled = true;
        
        return true;
    });
}