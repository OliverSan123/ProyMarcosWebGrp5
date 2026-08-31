// Esperamos a que el HTML termine de cargar por completo para que no busque elementos que no existen
document.addEventListener('DOMContentLoaded', () => {
    
    // Capturamos el botón de filtrar usando su ID
    const btnFiltrar = document.getElementById('btnFiltrar');

    // Le agregamos un evento para que ejecute todo este bloque apenas el usuario haga clic en "Filtrar"
    btnFiltrar.addEventListener('click', () => {

        // Obetenemos lo que el usuario seleccionó en los desplegables y lo pasa a minúsculas
        const prioridad = document.getElementById('filtroPrioridad').value.toLowerCase();
        const estado = document.getElementById('filtroEstado').value.toLowerCase();

        // Captura todas las filas de la tabla de tickets
        const filas = document.querySelectorAll('#tablaTickets .fila-ticket');

        // Recorremos la tabla fila por fila
        filas.forEach(fila => {
            // Estrae el texto de la columna Prioridad y columna Estado 
            const txtPrioridad = fila.cells[2].textContent.toLowerCase();
            const txtEstado = fila.cells[4].textContent.toLowerCase();

            // Comprueba si la fila cumple con la prioridad seleccionada
            const coincidePrioridad = prioridad.includes('todas') || txtPrioridad.includes(prioridad);
            
            // Comprueba si la fila cumple con el estado seleccionado o si eligió "Todos"
            const coincideEstado = estado.includes('todos') || txtEstado.includes(estado);

            // Si cumple ambos filtros, deja el display normal ('') para mostrar la fila.
            // Si no cumple, le asigna 'none'.
            fila.style.display = (coincidePrioridad && coincideEstado) ? '' : 'none';
        });
    });
});