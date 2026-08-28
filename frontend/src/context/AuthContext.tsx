"use client";

import React, { createContext, useContext, useEffect, useState, ReactNode, useCallback, useRef } from "react";
import { AccountDto } from "../types";
import AuthService from "../services/auth.service";
import AccountService from "../services/account.service";

interface AuthContextType {
	user: AccountDto | null;
	loading: boolean;
	login: (identifier: string, password: string, remember?: boolean) => Promise<boolean>;
	logout: () => Promise<void>;
	refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
	const [user, setUser] = useState<AccountDto | null>(null);
	const [loading, setLoading] = useState(true);
	const refreshPromise = useRef<Promise<void> | null>(null);

	const refreshUser = useCallback(async () => {
		if (refreshPromise.current) return refreshPromise.current;
		const promise = (async () => {
			try {
				const res = await AuthService.status();
				if (res.ok) {
					const meRes = await AccountService.me();
					setUser(meRes.ok ? await meRes.json() : null);
				} else {
					setUser(null);
				}
			} catch {
				setUser(null);
			}
		})();
		refreshPromise.current = promise;
		try {
			await promise;
		} finally {
			refreshPromise.current = null;
		}
	}, []);

	useEffect(() => {
		refreshUser().finally(() => setLoading(false));
	}, [refreshUser]);

	const login = async (identifier: string, password: string, remember = false) => {
		const res = await AuthService.login(identifier, password, remember);
		if (res.ok) {
			await refreshUser();
			return true;
		}
		return false;
	};

	const logout = async () => {
		await AuthService.logout();
		setUser(null);
	};

	return (
		<AuthContext.Provider value={{ user, loading, login, logout, refreshUser }}>{children}</AuthContext.Provider>
	);
}

export function useAuth() {
	const context = useContext(AuthContext);
	if (!context) {
		throw new Error("useAuth must be used within an AuthProvider");
	}
	return context;
}
