import { useState } from "react";
import {
    Box,
    Card,
    CardContent,
    TextField,
    Button,
    Typography,
    Stack,
    Alert,
    Tabs,
    Tab,
    Divider,
} from "@mui/material";
import { setToken, API_BASE } from "./api";

function LoginPage({ onLogin }) {
    const [tab, setTab] = useState(0);
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [loading, setLoading] = useState(false);

    const handleSubmit = async () => {
        setError("");
        setSuccess("");
        if (!username.trim() || !password.trim()) {
            setError("Please enter both username and password");
            return;
        }

        setLoading(true);
        const isLogin = tab === 0;
        const url = isLogin ? `${API_BASE}/auth/login` : `${API_BASE}/auth/register`;

        try {
            const res = await fetch(url, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ username, password }),
            });

            if (isLogin) {
                if (res.ok) {
                    const data = await res.json();
                    setToken(data.token);
                    onLogin();
                } else if (res.status === 404) {
                    setError("User not found");
                } else if (res.status === 401) {
                    setError("Wrong password");
                } else {
                    setError("Login failed");
                }
            } else {
                if (res.ok) {
                    setSuccess("Registered! Please login.");
                    setTab(0);
                    setPassword("");
                } else {
                    setError("Registration failed");
                }
            }
        } catch (e) {
            setError("Server error");
        }
        setLoading(false);
    };

    return (
        <Box
            sx={{
                minHeight: "100vh",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                background: "linear-gradient(160deg, #1B2A4A 0%, #2D4A7A 40%, #FAF6F0 100%)",
                position: "relative",
                overflow: "hidden",
            }}
        >
            {/* Decorative circle — like a rising sun */}
            <Box
                sx={{
                    position: "absolute",
                    width: 500,
                    height: 500,
                    borderRadius: "50%",
                    background: "radial-gradient(circle, rgba(197,61,67,0.15) 0%, transparent 70%)",
                    top: -100,
                    right: -100,
                    pointerEvents: "none",
                }}
            />

            <Card
                elevation={0}
                sx={{
                    width: 420,
                    borderRadius: 4,
                    border: "1px solid rgba(27, 42, 74, 0.08)",
                    boxShadow: "0 8px 40px rgba(27, 42, 74, 0.12)",
                    overflow: "visible",
                }}
            >
                <CardContent sx={{ p: 4 }}>
                    <Box sx={{ textAlign: "center", mb: 3 }}>
                        <Typography
                            sx={{
                                fontSize: 48,
                                lineHeight: 1,
                                mb: 1,
                                color: "#C53D43",
                                fontFamily: '"Noto Serif JP", serif',
                            }}
                        >
                            一生懸命
                        </Typography>
                        <Typography
                            variant="h5"
                            sx={{
                                fontFamily: '"Noto Serif JP", serif',
                                fontWeight: 700,
                                color: "#1B2A4A",
                                letterSpacing: 1,
                            }}
                        >
                            Japanese Note
                        </Typography>
                        <Typography variant="body2" sx={{ color: "#6B6B6B", mt: 0.5 }}>
                            Your personal study companion
                        </Typography>
                    </Box>

                    <Divider sx={{ mb: 2, borderColor: "rgba(197, 61, 67, 0.2)" }} />

                    <Tabs
                        value={tab}
                        onChange={(e, v) => { setTab(v); setError(""); setSuccess(""); }}
                        centered
                        sx={{
                            mb: 3,
                            "& .MuiTab-root": {
                                fontWeight: 600,
                                fontSize: "0.95rem",
                                color: "#6B6B6B",
                                "&.Mui-selected": { color: "#C53D43" },
                            },
                            "& .MuiTabs-indicator": { backgroundColor: "#C53D43", height: 3, borderRadius: 2 },
                        }}
                    >
                        <Tab label="Login" />
                        <Tab label="Register" />
                    </Tabs>

                    {error && <Alert severity="error" sx={{ mb: 2, borderRadius: 2 }}>{error}</Alert>}
                    {success && <Alert severity="success" sx={{ mb: 2, borderRadius: 2 }}>{success}</Alert>}

                    <Stack spacing={2.5}>
                        <TextField
                            label="Username"
                            value={username}
                            onChange={e => setUsername(e.target.value)}
                            fullWidth
                            size="small"
                            sx={{
                                "& .MuiOutlinedInput-root": {
                                    borderRadius: 2,
                                    "&.Mui-focused fieldset": { borderColor: "#C53D43" },
                                },
                                "& .MuiInputLabel-root.Mui-focused": { color: "#C53D43" },
                            }}
                        />
                        <TextField
                            label="Password"
                            type="password"
                            value={password}
                            onChange={e => setPassword(e.target.value)}
                            fullWidth
                            size="small"
                            onKeyDown={e => e.key === "Enter" && handleSubmit()}
                            sx={{
                                "& .MuiOutlinedInput-root": {
                                    borderRadius: 2,
                                    "&.Mui-focused fieldset": { borderColor: "#C53D43" },
                                },
                                "& .MuiInputLabel-root.Mui-focused": { color: "#C53D43" },
                            }}
                        />
                        <Button
                            variant="contained"
                            onClick={handleSubmit}
                            disabled={loading}
                            fullWidth
                            sx={{
                                borderRadius: 2,
                                py: 1.2,
                                backgroundColor: "#C53D43",
                                fontWeight: 600,
                                fontSize: "0.95rem",
                                letterSpacing: 0.5,
                                "&:hover": { backgroundColor: "#A83238" },
                            }}
                        >
                            {tab === 0 ? "Login" : "Register"}
                        </Button>
                    </Stack>
                </CardContent>
            </Card>
        </Box>
    );
}

export default LoginPage;
