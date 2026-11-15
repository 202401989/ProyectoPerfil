const express = require('express');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const { pool, sql } = require('../config/db');

const router = express.Router();

// REGISTRO DE USUARIO
router.post('/registro', async (req, res) => {
    try {
        const { nombre, apellido, correo, password } = req.body;

        if (!nombre || !apellido || !correo || !password) {
            return res.status(400).json({
                mensaje: 'Todos los campos son obligatorios'
            });
        }

        if (password.length < 8) {
            return res.status(400).json({
                mensaje: 'La contraseña debe tener al menos 8 caracteres'
            });
        }

        const usuarioExistente = await pool.request()
            .input('correo', sql.NVarChar(150), correo)
            .query('SELECT id_usuario FROM Usuarios WHERE correo = @correo');

        if (usuarioExistente.recordset.length > 0) {
            return res.status(409).json({
                mensaje: 'El correo ya está registrado'
            });
        }

        const passwordHash = await bcrypt.hash(password, 10);

        const resultado = await pool.request()
            .input('nombre', sql.NVarChar(100), nombre)
            .input('apellido', sql.NVarChar(100), apellido)
            .input('correo', sql.NVarChar(150), correo)
            .input('password_hash', sql.NVarChar(255), passwordHash)
            .query(`
                INSERT INTO Usuarios
                (nombre, apellido, correo, password_hash)
                OUTPUT INSERTED.id_usuario
                VALUES
                (@nombre, @apellido, @correo, @password_hash)
            `);

        res.status(201).json({
            mensaje: 'Usuario registrado correctamente',
            id_usuario: resultado.recordset[0].id_usuario
        });

    } catch (error) {
        console.error('Error en registro:', error.message);
        res.status(500).json({
            mensaje: 'Error al registrar el usuario'
        });
    }
});

// INICIO DE SESIÓN
router.post('/login', async (req, res) => {
    try {
        const { correo, password } = req.body;

        if (!correo || !password) {
            return res.status(400).json({
                mensaje: 'Correo y contraseña son obligatorios'
            });
        }

        const resultado = await pool.request()
            .input('correo', sql.NVarChar(150), correo)
            .query(`
                SELECT id_usuario, nombre, apellido, correo, password_hash
                FROM Usuarios
                WHERE correo = @correo
            `);

        if (resultado.recordset.length === 0) {
            return res.status(401).json({
                mensaje: 'Credenciales incorrectas'
            });
        }

        const usuario = resultado.recordset[0];

        const passwordValida = await bcrypt.compare(
            password,
            usuario.password_hash
        );

        if (!passwordValida) {
            return res.status(401).json({
                mensaje: 'Credenciales incorrectas'
            });
        }

        const token = jwt.sign(
            { id_usuario: usuario.id_usuario },
            process.env.JWT_SECRET,
            { expiresIn: '2h' }
        );

        res.json({
            mensaje: 'Inicio de sesión exitoso',
            token,
            usuario: {
                id_usuario: usuario.id_usuario,
                nombre: usuario.nombre,
                apellido: usuario.apellido,
                correo: usuario.correo
            }
        });

    } catch (error) {
        console.error('Error en login:', error.message);
        res.status(500).json({
            mensaje: 'Error al iniciar sesión'
        });
    }
});

module.exports = router;