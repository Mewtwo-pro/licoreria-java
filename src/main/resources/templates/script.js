
function toggleMenu(menuId) {
    const targetMenu = document.getElementById(menuId);
    
    // Lista de todos los menús desplegables
    const allMenus = ['managementDropdown', 'userDropdown'];
    
    // Cerramos el otro menú para que no se superpongan
    allMenus.forEach(id => {
        if (id !== menuId) {
            document.getElementById(id).classList.remove('show-menu');
        }
    });

    // Alternamos el menú actual
    targetMenu.classList.toggle('show-menu');
}

// Cierra cualquier menú si se hace clic fuera de ellos
window.addEventListener('click', function(event) {
    const managementBtn = document.getElementById('managementBtn');
    const managementDropdown = document.getElementById('managementDropdown');
    const userBtn = document.getElementById('userBtn');
    const userDropdown = document.getElementById('userDropdown');

    // Comprobamos el menú de gestión
    if (managementBtn && !managementBtn.contains(event.target) && !managementDropdown.contains(event.target)) {
        managementDropdown.classList.remove('show-menu');
    }

    // Comprobamos el menú de usuario
    if (userBtn && !userBtn.contains(event.target) && !userDropdown.contains(event.target)) {
        userDropdown.classList.remove('show-menu');
    }
});
