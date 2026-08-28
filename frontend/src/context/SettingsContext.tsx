"use client";

import React, { createContext, useContext, useEffect, useState, ReactNode } from "react";

type Theme = "light" | "dark" | "system";

interface SettingsContextType {
	theme: Theme;
	setTheme: (theme: Theme) => void;
	effectiveTheme: "light" | "dark";
}

const SettingsContext = createContext<SettingsContextType | undefined>(undefined);

export function SettingsProvider({ children }: { children: ReactNode }) {
	const [theme, setThemeState] = useState<Theme>("system");
	const [effectiveTheme, setEffectiveTheme] = useState<"light" | "dark">("light");

	useEffect(() => {
		const stored = localStorage.getItem("theme") as Theme | null;
		if (stored) {
			setThemeState(stored);
		}
	}, []);

	useEffect(() => {
		const updateEffectiveTheme = () => {
			if (theme === "system") {
				const systemDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
				setEffectiveTheme(systemDark ? "dark" : "light");
			} else {
				setEffectiveTheme(theme);
			}
		};

		updateEffectiveTheme();

		const mediaQuery = window.matchMedia("(prefers-color-scheme: dark)");
		mediaQuery.addEventListener("change", updateEffectiveTheme);

		return () => mediaQuery.removeEventListener("change", updateEffectiveTheme);
	}, [theme]);

	useEffect(() => {
		document.documentElement.classList.remove("light", "dark");
		document.documentElement.classList.add(effectiveTheme);
	}, [effectiveTheme]);

	const setTheme = (newTheme: Theme) => {
		setThemeState(newTheme);
		localStorage.setItem("theme", newTheme);
	};

	return <SettingsContext.Provider value={{ theme, setTheme, effectiveTheme }}>{children}</SettingsContext.Provider>;
}

export function useSettings() {
	const context = useContext(SettingsContext);
	if (!context) {
		throw new Error("useSettings must be used within a SettingsProvider");
	}
	return context;
}
