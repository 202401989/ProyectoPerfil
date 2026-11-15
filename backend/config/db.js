const sql = require('mssql/msnodesqlv8');

const config = {
    connectionString: `Driver={${process.env.DB_DRIVER}};Server=${process.env.DB_SERVER};Database=${process.env.DB_NAME};Trusted_Connection=Yes;TrustServerCertificate=Yes;`
};

const pool = new sql.ConnectionPool(config);

async function connectDB() {
    try {
        await pool.connect();
        console.log('Conexión exitosa con SQL Server');
        console.log('Base de datos: ProyectoPerfil');
        return pool;
    } catch (error) {
        console.error('Error al conectar con SQL Server:', error.message);
        throw error;
    }
}

module.exports = {
    sql,
    pool,
    connectDB
};