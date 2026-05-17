import { useState } from "react";
import {
    Box,
    Card,
    CardContent,
    TextField,
    Button,
    MenuItem,
    Typography,
    Stack,
} from "@mui/material";
import AddCircleOutlineIcon from "@mui/icons-material/AddCircleOutline";
import { apiFetch } from "./api";

const typeOptions = [
    { value: "vocab", label: "Vocab" },
    { value: "grammar", label: "Grammar" },
    { value: "other", label: "Other" },
];

function NoteForm({ onAdd, currentType }) {
    const [title, setTitle] = useState("");
    const [meaning, setMeaning] = useState("");
    const [example, setExample] = useState("");
    const [type, setType] = useState(currentType || "vocab");
    const [loading, setLoading] = useState(false);

    const handleSubmit = async () => {
        if (!title.trim()) return;
        setLoading(true);
        await apiFetch("/notes", {
            method: "POST",
            body: JSON.stringify({ title, meaning, example, type }),
        });
        setTitle("");
        setMeaning("");
        setExample("");
        setLoading(false);
        onAdd();
    };

    const fieldSx = {
        "& .MuiOutlinedInput-root": {
            borderRadius: 2,
            backgroundColor: "#FAFAF8",
            "&:hover fieldset": { borderColor: "#C53D43" },
            "&.Mui-focused fieldset": { borderColor: "#C53D43" },
        },
        "& .MuiInputLabel-root.Mui-focused": { color: "#C53D43" },
    };

    return (
        <Card
            elevation={0}
            sx={{
                mb: 3,
                borderRadius: 3,
                border: "1px solid rgba(27, 42, 74, 0.08)",
                boxShadow: "0 2px 12px rgba(27, 42, 74, 0.06)",
            }}
        >
            <CardContent sx={{ p: 3 }}>
                <Box sx={{ display: "flex", alignItems: "center", gap: 1, mb: 2 }}>
                    <Box
                        sx={{
                            width: 4,
                            height: 24,
                            backgroundColor: "#C53D43",
                            borderRadius: 2,
                        }}
                    />
                    <Typography
                        variant="subtitle1"
                        sx={{
                            fontFamily: '"Noto Serif JP", serif',
                            fontWeight: 700,
                            color: "#1B2A4A",
                        }}
                    >
                        Add Note
                    </Typography>
                </Box>
                <Stack spacing={2}>
                    <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
                        <TextField
                            label="Title"
                            value={title}
                            onChange={e => setTitle(e.target.value)}
                            size="small"
                            fullWidth
                            sx={fieldSx}
                        />
                        <TextField
                            select
                            label="Type"
                            value={type}
                            onChange={e => setType(e.target.value)}
                            size="small"
                            sx={{ minWidth: 130, ...fieldSx }}
                        >
                            {typeOptions.map(opt => (
                                <MenuItem key={opt.value} value={opt.value}>{opt.label}</MenuItem>
                            ))}
                        </TextField>
                    </Stack>
                    <TextField
                        label="Meaning"
                        value={meaning}
                        onChange={e => setMeaning(e.target.value)}
                        size="small"
                        fullWidth
                        sx={fieldSx}
                    />
                    <TextField
                        label="Example"
                        value={example}
                        onChange={e => setExample(e.target.value)}
                        size="small"
                        fullWidth
                        multiline
                        minRows={2}
                        sx={fieldSx}
                    />
                    <Box sx={{ display: "flex", justifyContent: "flex-end" }}>
                        <Button
                            variant="contained"
                            startIcon={<AddCircleOutlineIcon />}
                            onClick={handleSubmit}
                            disabled={loading || !title.trim()}
                            sx={{
                                borderRadius: 2,
                                px: 3,
                                backgroundColor: "#C53D43",
                                fontWeight: 600,
                                "&:hover": { backgroundColor: "#A83238" },
                            }}
                        >
                            Add
                        </Button>
                    </Box>
                </Stack>
            </CardContent>
        </Card>
    );
}

export default NoteForm;
