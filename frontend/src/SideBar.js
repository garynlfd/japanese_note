import {
    Drawer,
    List,
    ListItemButton,
    ListItemIcon,
    ListItemText,
    Toolbar,
    Typography,
    Box,
    Divider,
} from "@mui/material";
import AbcIcon from "@mui/icons-material/Abc";
import MenuBookIcon from "@mui/icons-material/MenuBook";
import StarIcon from "@mui/icons-material/Star";

const categories = [
    { label: "Vocab", japanese: "単語", value: "vocab", icon: <AbcIcon /> },
    { label: "Grammar", japanese: "文法", value: "grammar", icon: <MenuBookIcon /> },
    { label: "Other", japanese: "他", value: "other", icon: <StarIcon /> },
];

function Sidebar({ setType, currentType, drawerWidth }) {
    return (
        <Drawer
            variant="permanent"
            sx={{
                width: drawerWidth,
                flexShrink: 0,
                "& .MuiDrawer-paper": {
                    width: drawerWidth,
                    boxSizing: "border-box",
                    backgroundColor: "#1B2A4A",
                    color: "#E8E4DC",
                    borderRight: "none",
                },
            }}
        >
            <Toolbar />
            <Box sx={{ px: 2.5, py: 2 }}>
                <Typography
                    variant="caption"
                    sx={{
                        color: "rgba(232, 228, 220, 0.5)",
                        fontWeight: 600,
                        letterSpacing: 2,
                        fontSize: "0.7rem",
                    }}
                >
                    CATEGORIES
                </Typography>
            </Box>
            <Divider sx={{ borderColor: "rgba(232, 228, 220, 0.1)", mx: 2 }} />
            <List disablePadding sx={{ mt: 1 }}>
                {categories.map(({ label, japanese, value, icon }) => (
                    <ListItemButton
                        key={value}
                        selected={currentType === value}
                        onClick={() => setType(value)}
                        sx={{
                            mx: 1.5,
                            my: 0.5,
                            borderRadius: 2,
                            color: "#E8E4DC",
                            transition: "all 0.2s ease",
                            "&.Mui-selected": {
                                backgroundColor: "#C53D43",
                                color: "#fff",
                                "&:hover": { backgroundColor: "#A83238" },
                                "& .MuiListItemIcon-root": { color: "#fff" },
                            },
                            "&:hover": { backgroundColor: "rgba(232, 228, 220, 0.08)" },
                        }}
                    >
                        <ListItemIcon sx={{ color: "rgba(232, 228, 220, 0.6)", minWidth: 36 }}>
                            {icon}
                        </ListItemIcon>
                        <ListItemText
                            primary={label}
                            secondary={japanese}
                            primaryTypographyProps={{
                                fontWeight: currentType === value ? 700 : 400,
                                fontSize: "0.9rem",
                            }}
                            secondaryTypographyProps={{
                                sx: {
                                    color: currentType === value
                                        ? "rgba(255,255,255,0.7)"
                                        : "rgba(232, 228, 220, 0.4)",
                                    fontSize: "0.75rem",
                                    fontFamily: '"Noto Serif JP", serif',
                                },
                            }}
                        />
                    </ListItemButton>
                ))}
            </List>

            {/* Decorative bottom element */}
            <Box sx={{ flexGrow: 1 }} />
            <Box sx={{ textAlign: "center", pb: 3, opacity: 0.15 }}>
                <Typography
                    sx={{
                        fontFamily: '"Noto Serif JP", serif',
                        fontSize: 40,
                        color: "#E8E4DC",
                    }}
                >
                    学
                </Typography>
            </Box>
        </Drawer>
    );
}

export default Sidebar;
