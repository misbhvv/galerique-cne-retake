"use client";

import React, { useState, useEffect } from "react";
import Link from "next/link";
import Image from "next/image";
import { useAuth } from "../context/AuthContext";
import { Search, User, ShoppingBag, Plus, LogOut, Menu, X } from "lucide-react";

interface NavbarProps {
	onSearchClick?: () => void;
	onLoginClick?: () => void;
}

export default function Navbar({ onSearchClick, onLoginClick }: NavbarProps) {
	const { user, logout } = useAuth();
	const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
	const [scrolled, setScrolled] = useState(false);

	useEffect(() => {
		const handleScroll = () => setScrolled(window.scrollY > 10);
		window.addEventListener("scroll", handleScroll);
		return () => window.removeEventListener("scroll", handleScroll);
	}, []);

	const initials =
		user?.username
			?.split(" ")
			.map((n: string) => n[0])
			.join("")
			.toUpperCase()
			.slice(0, 2) || "?";

	return (
		<nav
			className={`fixed top-0 left-0 right-0 z-50 transition-all duration-700 ${
				scrolled
					? "bg-stone-50/80 dark:bg-stone-950/80 backdrop-blur-2xl border-b border-stone-200/60 dark:border-stone-800/60"
					: "bg-transparent"
			}`}
		>
			<div className="max-w-[1400px] mx-auto px-6 lg:px-10">
				<div className="flex items-center justify-between h-20">
					{/* Logo */}
					<Link
						href="/"
						className="flex items-center gap-3 group"
					>
						<div className="relative w-9 h-9 transition-transform duration-500 group-hover:scale-105">
							<Image
								src="/logo/brandmark.png"
								alt="Galerique"
								fill
								className="object-contain"
								priority
							/>
						</div>
						<span className="font-[var(--font-bricolage)] font-bold text-lg tracking-tight text-stone-900 dark:text-stone-100 hidden sm:block">
							Galerique
						</span>
					</Link>

					{/* Center Nav - Desktop */}
					<div className="hidden lg:flex items-center gap-8 absolute left-1/2 -translate-x-1/2">
						{[
							{ href: "/", label: "Gallery" },
							{ href: "/trending", label: "Trending" },
							{ href: "/about", label: "About" },
						].map(({ href, label }) => (
							<Link
								key={href}
								href={href}
								className="text-stone-500 dark:text-stone-400 hover:text-stone-900 dark:hover:text-stone-100 text-[13px] tracking-wide font-medium transition-colors duration-300"
							>
								{label}
							</Link>
						))}
					</div>

					{/* Right actions */}
					<div className="flex items-center gap-2">
						{/* Search */}
						<button
							onClick={onSearchClick}
							className="p-2.5 text-stone-500 dark:text-stone-400 hover:text-stone-900 dark:hover:text-stone-100 transition-colors duration-300"
							title="Search (⌘K)"
						>
							<Search
								size={18}
								strokeWidth={1.5}
							/>
						</button>

						{user ? (
							<>
								<Link
									href="/upload"
									className="hidden sm:flex items-center gap-2 px-5 py-2 text-stone-600 dark:text-stone-300 hover:text-stone-900 dark:hover:text-stone-100 text-[13px] font-medium transition-colors duration-300"
								>
									<Plus
										size={15}
										strokeWidth={1.5}
									/>
									Upload
								</Link>
								<Link
									href="/purchases"
									className="p-2.5 text-stone-500 dark:text-stone-400 hover:text-stone-900 dark:hover:text-stone-100 transition-colors duration-300"
									title="Collection"
								>
									<ShoppingBag
										size={18}
										strokeWidth={1.5}
									/>
								</Link>
								<Link
									href={`/profile/${user.id}`}
									className="w-8 h-8 rounded-full bg-stone-200 dark:bg-stone-800 flex items-center justify-center text-[11px] font-semibold text-stone-600 dark:text-stone-300 hover:bg-stone-300 dark:hover:bg-stone-700 transition-colors duration-300"
									title="Profile"
								>
									{initials}
								</Link>
								<button
									onClick={logout}
									className="p-2.5 text-stone-400 hover:text-red-500 transition-colors duration-300"
									title="Sign out"
								>
									<LogOut
										size={16}
										strokeWidth={1.5}
									/>
								</button>
							</>
						) : (
							<>
								<button
									onClick={onLoginClick}
									className="hidden sm:block text-[13px] font-medium text-stone-500 dark:text-stone-400 hover:text-stone-900 dark:hover:text-stone-100 transition-colors duration-300 px-4 py-2"
								>
									Sign in
								</button>
								<button
									onClick={onLoginClick}
									className="hidden sm:flex items-center px-5 py-2 bg-stone-900 dark:bg-stone-100 text-white dark:text-stone-900 rounded-full text-[13px] font-medium hover:bg-stone-800 dark:hover:bg-stone-200 transition-all duration-300"
								>
									Join
								</button>
							</>
						)}

						{/* Mobile toggle */}
						<button
							onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
							className="lg:hidden p-2.5 text-stone-500 dark:text-stone-400 hover:text-stone-900 dark:hover:text-stone-100 transition-colors duration-300"
						>
							{mobileMenuOpen ? <X size={18} /> : <Menu size={18} />}
						</button>
					</div>
				</div>
			</div>

			{/* Mobile menu */}
			<div
				className={`lg:hidden overflow-hidden transition-all duration-500 ease-out ${
					mobileMenuOpen ? "max-h-96 opacity-100" : "max-h-0 opacity-0"
				}`}
			>
				<div className="bg-stone-50/95 dark:bg-stone-950/95 backdrop-blur-2xl border-t border-stone-200/50 dark:border-stone-800/50 px-6 py-8">
					<div className="flex flex-col gap-1">
						{[
							{ href: "/", label: "Gallery" },
							{ href: "/trending", label: "Trending" },
							{ href: "/about", label: "About" },
						].map(({ href, label }) => (
							<Link
								key={href}
								href={href}
								className="px-4 py-3 text-stone-600 dark:text-stone-300 hover:text-stone-900 dark:hover:text-stone-100 font-medium transition-colors"
								onClick={() => setMobileMenuOpen(false)}
							>
								{label}
							</Link>
						))}
						{user ? (
							<>
								<div className="h-px bg-stone-200 dark:bg-stone-800 my-3" />
								<Link
									href="/purchases"
									className="px-4 py-3 text-stone-600 dark:text-stone-300 hover:text-stone-900 dark:hover:text-stone-100 font-medium transition-colors flex items-center gap-3"
									onClick={() => setMobileMenuOpen(false)}
								>
									<ShoppingBag
										size={16}
										strokeWidth={1.5}
									/>
									Collection
								</Link>
								<Link
									href={`/profile/${user.id}`}
									className="px-4 py-3 text-stone-600 dark:text-stone-300 hover:text-stone-900 dark:hover:text-stone-100 font-medium transition-colors flex items-center gap-3"
									onClick={() => setMobileMenuOpen(false)}
								>
									<User
										size={16}
										strokeWidth={1.5}
									/>
									Profile
								</Link>
								<Link
									href="/upload"
									className="mt-3 px-4 py-3 bg-stone-900 dark:bg-stone-100 text-white dark:text-stone-900 rounded-xl text-center font-medium flex items-center justify-center gap-2"
									onClick={() => setMobileMenuOpen(false)}
								>
									<Plus
										size={16}
										strokeWidth={1.5}
									/>
									Upload Artwork
								</Link>
							</>
						) : (
							<>
								<div className="h-px bg-stone-200 dark:bg-stone-800 my-3" />
								<button
									onClick={() => {
										setMobileMenuOpen(false);
										onLoginClick?.();
									}}
									className="mt-3 px-4 py-3 bg-stone-900 dark:bg-stone-100 text-white dark:text-stone-900 rounded-xl text-center font-medium"
								>
									Sign in
								</button>
							</>
						)}
					</div>
				</div>
			</div>
		</nav>
	);
}
