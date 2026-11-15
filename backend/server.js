require('dotenv').config();

const express = require('express');
const cors = require('cors');
const { connectDB } = require('./config/db');

const authRoutes = require('./routes/auth');

const app = express();

// Configuración de middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Ruta principal
app.get('/', (req, res) => {
    res.json({
        mensaje: 'Backend ProyectoPerfil funcionando correctamente'
    });
});

// Rutas de autenticación
app.use('/api/auth', authRoutes);

// Puerto del servidor
const PORT = process.env.PORT || 3000;

// Iniciar servidor
async function iniciarServidor() {
    try {
        await connectDB();

        app.listen(PORT, () => {
            console.log(`Servidor funcionando en http://localhost:${PORT}`);
        });

    } catch (error) {
        console.error('No se pudo iniciar el servidor.');
        process.exit(1);
    }
}

iniciarServidor();