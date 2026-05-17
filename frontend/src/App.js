import { useState, useEffect } from "react";
import {
    Box, CssBaseline, Toolbar, AppBar, Typography,
    createTheme, ThemeProvider, TextField, InputAdornment,
    IconButton, Pagination, Stack,
} from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import LogoutIcon from "@mui/icons-material/Logout";
import Sidebar from './SideBar';
import NoteForm from './NoteForm';
import NoteList from './NoteList';
import LoginPage from './LoginPage';
import { apiFetch, getToken, removeToken } from './api';

const DRAWER_WIDTH = 240;

const theme = createTheme({
    palette: {
        primary: { main: "#C53D43", light: "#E8828A", dark: "#8B2930" },       // 朱色 vermillion
        secondary: { main: "#1B2A4A", light: "#2D4A7A", dark: "#0F1A30" },     // 紺色 indigo
        background: { default: "#FAF6F0", paper: "#FFFFFF" },                   // 生成り kinari
        text: { primary: "#2C2C2C", secondary: "#6B6B6B" },
        error: { main: "#C53D43" },
    },
    typography: {
        fontFamily: '"Noto Sans JP", "Noto Sans TC", "Roboto", sans-serif',
        h4: { fontFamily: '"Noto Serif JP", "Noto Sans JP", serif' },
        h5: { fontFamily: '"Noto Serif JP", "Noto Sans JP", serif' },
        h6: { fontFamily: '"Noto Serif JP", "Noto Sans JP", serif' },
    },
    shape: { borderRadius: 8 },
    components: {
        MuiButton: {
            styleOverrides: {
                root: { textTransform: "none", fontWeight: 500 },
            },
        },
        MuiCard: {
            styleOverrides: {
                root: { borderRadius: 12 },
            },
        },
    },
});

function App() {
    const [loggedIn, setLoggedIn] = useState(!!getToken());
    const [notes, setNotes] = useState([]);
    const [type, setType] = useState("vocab");
    const [searchStr, setSearchStr] = useState("");
    const [curPage, setCurPage] = useState(1);
    const [numOfPages, setNumOfPages] = useState(1);
    const [total, setTotal] = useState(0);

    const fetchNotes = async () => {
        let url = `/notes?type=${type}&curPage=${curPage}&pageSize=12`;
        if (searchStr.trim()) {
            url += `&searchStr=${encodeURIComponent(searchStr)}`;
        }
        const res = await apiFetch(url);
        if (!res) return;
        const data = await res.json();
        setNotes(data.data || []);
        setNumOfPages(data.numOfPages || 1);
        setTotal(data.total || 0);
    };

    useEffect(() => {
        if (loggedIn) fetchNotes();
    }, [type, curPage, loggedIn]);

    const handleSearch = () => {
        setCurPage(1);
        fetchNotes();
    };

    const handleTypeChange = (newType) => {
        setType(newType);
        setCurPage(1);
        setSearchStr("");
    };

    const handleLogout = () => {
        removeToken();
        setLoggedIn(false);
    };

    if (!loggedIn) {
        return (
            <ThemeProvider theme={theme}>
                <CssBaseline />
                <LoginPage onLogin={() => setLoggedIn(true)} />
            </ThemeProvider>
        );
    }

    return (
        <ThemeProvider theme={theme}>
            <CssBaseline />
            <Box sx={{ display: "flex", minHeight: "100vh" }}>
                <AppBar
                    position="fixed"
                    elevation={0}
                    sx={{
                        zIndex: (t) => t.zIndex.drawer + 1,
                        backgroundColor: "#1B2A4A",
                        borderBottom: "2px solid #C53D43",
                    }}
                >
                    <Toolbar>
                        <Typography
                            variant="h6"
                            sx={{
                                flexGrow: 1,
                                fontFamily: '"Noto Serif JP", serif',
                                fontWeight: 700,
                                letterSpacing: 2,
                            }}
                        >
                            <Box component="span" sx={{ color: "#C53D43", mr: 1.5, fontSize: "1.2em" }}>
								一生懸命
                            </Box>
                            Japanese Note
                        </Typography>
                        <IconButton
                            color="inherit"
                            onClick={handleLogout}
                            title="Logout"
                            sx={{
                                "&:hover": { backgroundColor: "rgba(197, 61, 67, 0.2)" },
                            }}
                        >
                            <LogoutIcon />
                        </IconButton>
                    </Toolbar>
                </AppBar>

                <Sidebar setType={handleTypeChange} currentType={type} drawerWidth={DRAWER_WIDTH} />

                <Box
                    component="main"
                    sx={{
                        flexGrow: 1,
                        p: 3,
                        backgroundColor: "background.default",
                        minHeight: "100vh",
                    }}
                >
                    <Toolbar />
                    <NoteForm onAdd={() => { setCurPage(1); fetchNotes(); }} currentType={type} />

                    <Stack direction="row" spacing={2} alignItems="center" sx={{ mb: 2 }}>
                        <TextField
                            placeholder="Search notes..."
                            value={searchStr}
                            onChange={e => setSearchStr(e.target.value)}
                            onKeyDown={e => e.key === "Enter" && handleSearch()}
                            size="small"
                            sx={{
                                width: 320,
                                backgroundColor: "#fff",
                                borderRadius: 2,
                                "& .MuiOutlinedInput-root": {
                                    borderRadius: 2,
                                    "&:hover fieldset": { borderColor: "#C53D43" },
                                    "&.Mui-focused fieldset": { borderColor: "#C53D43" },
                                },
                            }}
                            InputProps={{
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <SearchIcon sx={{ color: "#C53D43" }} />
                                    </InputAdornment>
                                ),
                            }}
                        />
                        <Typography variant="body2" sx={{ color: "#6B6B6B", fontWeight: 500 }}>
                            {total} notes
                        </Typography>
                    </Stack>

                    <NoteList notes={notes} onDelete={() => fetchNotes()} />

                    {numOfPages > 1 && (
                        <Box sx={{ display: "flex", justifyContent: "center", mt: 4, mb: 2 }}>
                            <Pagination
                                count={numOfPages}
                                page={curPage}
                                onChange={(e, page) => setCurPage(page)}
                                color="primary"
                                sx={{
                                    "& .MuiPaginationItem-root": {
                                        fontWeight: 500,
                                        "&.Mui-selected": {
                                            backgroundColor: "#C53D43",
                                            color: "#fff",
                                        },
                                    },
                                }}
                            />
                        </Box>
                    )}
                </Box>
            </Box>
        </ThemeProvider>
    );
}

export default App;
