/**
 * cart/order-success.html JavaScript
 */


        // Print order function
        function printOrder() {
            window.print();
        }
        
        // Auto refresh status every 30 seconds
        setInterval(function() {
            // This would typically call an API to check order status
            // For demo purposes, we'll just log
            console.log('Checking order status...');
        }, 30000);
        
        // Confetti effect
        function createConfetti() {
            const colors = ['#ff6b6b', '#4ecdc4', '#45b7d1', '#96ceb4', '#feca57'];
            
            for (let i = 0; i < 50; i++) {
                const confetti = document.createElement('div');
                confetti.style.cssText = `
                    position: fixed;
                    top: -10px;
                    left: ${Math.random() * 100}vw;
                    width: ${Math.random() * 10 + 5}px;
                    height: ${Math.random() * 10 + 5}px;
                    background: ${colors[Math.floor(Math.random() * colors.length)]};
                    animation: fall ${Math.random() * 3 + 2}s linear forwards;
                    z-index: 10000;
                `;
                
                document.body.appendChild(confetti);
                
                setTimeout(() => {
                    confetti.remove();
                }, 5000);
            }
        }
        
        // Add confetti CSS animation
        const style = document.createElement('style');
        style.textContent = `
            @keyframes fall {
                to {
                    transform: translateY(100vh) rotate(360deg);
                }
            }
        `;
        document.head.appendChild(style);
        
        // Trigger confetti on page load
        document.addEventListener('DOMContentLoaded', function() {
            setTimeout(createConfetti, 500);
        });
    