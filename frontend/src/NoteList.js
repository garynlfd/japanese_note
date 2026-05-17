import {
    Card,
    CardContent,
    Typography,
    Grid,
    Chip,
    Box,
    Divider,
    IconButton,
} from "@mui/material";
import TranslateIcon from "@mui/icons-material/Translate";
import FormatQuoteIcon from "@mui/icons-material/FormatQuote";
import DeleteOutlineIcon from "@mui/icons-material/DeleteOutline";
import { apiFetch } from "./api";

const typeConfig = {
    vocab: { label: "単語", color: "#C53D43", bg: "rgba(197, 61, 67, 0.08)" },
    grammar: { label: "文法", color: "#1B2A4A", bg: "rgba(27, 42, 74, 0.08)" },
    other: { label: "他", color: "#7B6B4E", bg: "rgba(123, 107, 78, 0.08)" },
};

function NoteList({ notes, onDelete }) {
    const handleDelete = async (id) => {
        await apiFetch(`/notes/${id}`, { method: "DELETE" });
        onDelete();
    };

    if (notes.length === 0) {
        return (
            <Box sx={{ textAlign: "center", mt: 10, color: "text.secondary" }}>
                <Typography
                    sx={{
                        fontFamily: '"Noto Serif JP", serif',
                        fontSize: 64,
                        color: "rgba(27, 42, 74, 0.1)",
                        mb: 2,
                    }}
                >
                    空
                </Typography>
                <Typography variant="h6" sx={{ fontFamily: '"Noto Serif JP", serif', color: "#6B6B6B" }}>
                    No notes yet
                </Typography>
                <Typography variant="body2" sx={{ color: "#999", mt: 0.5 }}>
                    Add a new note using the form above.
                </Typography>
            </Box>
        );
    }

    return (
        <Grid container spacing={2.5}>
            {notes.map(note => {
                const config = typeConfig[note.type] || typeConfig.other;
                return (
                    <Grid item xs={12} sm={6} md={4} key={note.id}>
                        <Card
                            elevation={0}
                            sx={{
                                height: "100%",
                                borderRadius: 3,
                                border: "1px solid rgba(27, 42, 74, 0.06)",
                                boxShadow: "0 2px 8px rgba(27, 42, 74, 0.04)",
                                transition: "all 0.25s ease",
                                position: "relative",
                                overflow: "visible",
                                "&:hover": {
                                    boxShadow: "0 8px 24px rgba(27, 42, 74, 0.1)",
                                    transform: "translateY(-2px)",
                                },
                                "&::before": {
                                    content: '""',
                                    position: "absolute",
                                    top: 0,
                                    left: 0,
                                    right: 0,
                                    height: 3,
                                    backgroundColor: config.color,
                                    borderRadius: "12px 12px 0 0",
                                },
                            }}
                        >
                            <CardContent sx={{ p: 2.5 }}>
                                <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", mb: 1.5 }}>
                                    <Typography
                                        variant="h6"
                                        sx={{
                                            fontFamily: '"Noto Serif JP", serif',
                                            fontWeight: 700,
                                            fontSize: "1.1rem",
                                            flex: 1,
                                            mr: 1,
                                            color: "#1B2A4A",
                                        }}
                                    >
                                        {note.title}
                                    </Typography>
                                    <Chip
                                        label={config.label}
                                        size="small"
                                        sx={{
                                            fontWeight: 600,
                                            fontSize: "0.7rem",
                                            fontFamily: '"Noto Sans JP", sans-serif',
                                            backgroundColor: config.bg,
                                            color: config.color,
                                            border: "none",
                                        }}
                                    />
                                </Box>

                                {note.meaning && (
                                    <>
                                        <Divider sx={{ my: 1.5, borderColor: "rgba(27, 42, 74, 0.06)" }} />
                                        <Box sx={{ display: "flex", gap: 1, alignItems: "flex-start" }}>
                                            <TranslateIcon
                                                fontSize="small"
                                                sx={{ mt: 0.2, color: "#C53D43", opacity: 0.7, fontSize: 18 }}
                                            />
                                            <Typography variant="body2" sx={{ color: "#4A4A4A", lineHeight: 1.6 }}>
                                                {note.meaning}
                                            </Typography>
                                        </Box>
                                    </>
                                )}

                                {note.example && (
                                    <Box
                                        sx={{
                                            display: "flex",
                                            gap: 1,
                                            alignItems: "flex-start",
                                            mt: 1.5,
                                            p: 1.5,
                                            backgroundColor: "#FAF6F0",
                                            borderRadius: 2,
                                            borderLeft: "3px solid rgba(197, 61, 67, 0.3)",
                                        }}
                                    >
                                        <FormatQuoteIcon
                                            fontSize="small"
                                            sx={{ mt: 0.1, color: "#C53D43", opacity: 0.5, fontSize: 18 }}
                                        />
                                        <Typography
                                            variant="body2"
                                            sx={{
                                                color: "#5A5A5A",
                                                fontStyle: "italic",
                                                lineHeight: 1.6,
                                            }}
                                        >
                                            {note.example}
                                        </Typography>
                                    </Box>
                                )}

                                <Box sx={{ display: "flex", justifyContent: "flex-end", mt: 1.5 }}>
                                    <IconButton
                                        size="small"
                                        onClick={() => handleDelete(note.id)}
                                        sx={{
                                            color: "rgba(0,0,0,0.2)",
                                            transition: "all 0.2s ease",
                                            "&:hover": {
                                                color: "#C53D43",
                                                backgroundColor: "rgba(197, 61, 67, 0.08)",
                                            },
                                        }}
                                    >
                                        <DeleteOutlineIcon fontSize="small" />
                                    </IconButton>
                                </Box>
                            </CardContent>
                        </Card>
                    </Grid>
                );
            })}
        </Grid>
    );
}

export default NoteList;
