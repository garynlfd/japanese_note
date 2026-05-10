require("dotenv").config();
const express = require("express");
const { Pool } = require("pg");
const cors = require("cors");

const app = express();
app.use(cors());
app.use(express.json());

const pool = new Pool({
  	user: process.env.DB_USER,
  	host: process.env.DB_HOST,
  	database: process.env.DB_NAME,
  	password: process.env.DB_PASSWORD,
  	port: process.env.DB_PORT,
});

app.get("/api/notes", async (req, res) => {
	const { type } = req.query;
	const result = await pool.query(
			"SELECT * FROM notes WHERE type=$1 ORDER BY created_at DESC",
			[type]
	);
	res.json(result.rows);
});

app.post("/api/notes", async (req, res) => {
	const { type, title, meaning, example } = req.body;
	const result = await pool.query(
			"INSERT INTO notes (type, title, meaning, example) VALUES ($1,$2,$3,$4) RETURNING *",
			[type, title, meaning, example]
	);
	res.json(result.rows[0]);
});

app.listen(process.env.PORT, () => console.log(`Server running on port ${process.env.PORT}`));