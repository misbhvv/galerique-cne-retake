"use client";

import React, { useState, useEffect, useCallback } from "react";
import { useRouter } from "next/navigation";
import { AuthProvider } from "../context/AuthContext";
import Navbar from "./Navbar";
import Footer from "./Footer";
import AuthModal from "./AuthModal";
import SearchModal from "./SearchModal";

export default function ClientLayout({ children }: { children: React.ReactNode }) {
	const [authModalOpen, setAuthModalOpen] = useState(false);
	const [searchModalOpen, setSearchModalOpen] = useState(false);
	const [searchInitialQuery, setSearchInitialQuery] = useState("");
	const router = useRouter();

	const openAuthModal = useCallback(() => setAuthModalOpen(true), []);
	const closeAuthModal = useCallback(() => setAuthModalOpen(false), []);
	const openSearch = useCallback((query: string = "") => {
		setSearchInitialQuery(query);
		setSearchModalOpen(true);
	}, []);
	const closeSearch = useCallback(() => setSearchModalOpen(false), []);

	useEffect(() => {
		const params = new URLSearchParams(window.location.search);
		if (params.get("searchbox") === "true") {
			openSearch(params.get("query") || "");
		}
	}, [openSearch]);

	// Global keyboard shortcuts
	useEffect(() => {
		const handleKeyDown = (e: KeyboardEvent) => {
			const target = e.target as HTMLElement;
			const isInput = target.tagName === "INPUT" || target.tagName === "TEXTAREA" || target.isContentEditable;

			// Cmd/Ctrl + K → open search
			if ((e.metaKey || e.ctrlKey) && e.key === "k") {
				e.preventDefault();
				if (searchModalOpen) {
					closeSearch();
				} else {
					openSearch();
				}
				return;
			}

			// Ctrl/Cmd + F → open search modal instead of browser find
			if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === "f") {
				e.preventDefault();
				openSearch();
				return;
			}

			// Escape → close modals
			if (e.key === "Escape") {
				if (searchModalOpen) {
					closeSearch();
					return;
				}
				if (authModalOpen) {
					closeAuthModal();
					return;
				}
			}

			// Don't trigger letter shortcuts when typing in inputs
			if (isInput) return;

			// U → open upload
			if (e.key === "u" || e.key === "U") {
				e.preventDefault();
				router.push("/upload");
				return;
			}

			// H → go home
			if (e.key === "h" || e.key === "H") {
				e.preventDefault();
				router.push("/");
				return;
			}
		};
		window.addEventListener("keydown", handleKeyDown);
		return () => window.removeEventListener("keydown", handleKeyDown);
	}, [searchModalOpen, authModalOpen, closeSearch, closeAuthModal, openSearch, router]);

	return (
		<AuthProvider>
			<div className="min-h-screen flex flex-col">
				<Navbar
					onSearchClick={() => openSearch()}
					onLoginClick={openAuthModal}
				/>
				<main className="flex-1 pt-20">{children}</main>
				<Footer />
			</div>

			<AuthModal
				isOpen={authModalOpen}
				onClose={closeAuthModal}
			/>
			<SearchModal
				isOpen={searchModalOpen}
				initialQuery={searchInitialQuery}
				onClose={closeSearch}
			/>
		</AuthProvider>
	);
}
