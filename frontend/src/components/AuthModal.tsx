"use client";

import React, { useState } from "react";
import Image from "next/image";
import { X, Eye, EyeOff } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import AccountService from "../services/account.service";

interface AuthModalProps {
	isOpen: boolean;
	onClose: () => void;
}

export default function AuthModal({ isOpen, onClose }: AuthModalProps) {
	const [mode, setMode] = useState<"login" | "signup">("login");
	const [identifier, setIdentifier] = useState("");
	const [email, setEmail] = useState("");
	const [password, setPassword] = useState("");
	const [username, setUsername] = useState("");
	const [showPassword, setShowPassword] = useState(false);
	const [remember, setRemember] = useState(false);
	const [loading, setLoading] = useState(false);
	const [error, setError] = useState("");

	const { login } = useAuth();

	const handleSubmit = async (e: React.FormEvent) => {
		e.preventDefault();
		setError("");
		setLoading(true);

		try {
			if (mode === "signup") {
				const res = await AccountService.create({ username, email, password });
				if (!res.ok) {
					let message = "Failed to create account";
					try {
						const data = await res.json();
						message = data.message || message;
					} catch {
						// response may not have JSON body
					}
					throw new Error(message);
				}
			}

			const loginIdentifier = mode === "signup" ? email : identifier;
			const success = await login(loginIdentifier, password, remember);
			if (success) {
				onClose();
				setIdentifier("");
				setEmail("");
				setPassword("");
				setUsername("");
			} else {
				throw new Error("Invalid credentials");
			}
		} catch (err: any) {
			setError(err.message || "Something went wrong");
		} finally {
			setLoading(false);
		}
	};

	if (!isOpen) return null;

	return (
		<div className="fixed inset-0 z-[100] flex items-center justify-center">
			<div
				className="absolute inset-0 bg-black/40 backdrop-blur-sm"
				onClick={onClose}
			/>

			<div className="relative w-full max-w-md mx-4 bg-white dark:bg-stone-900 rounded-xl shadow-2xl overflow-hidden border border-stone-200 dark:border-stone-800">
				<button
					onClick={onClose}
					className="absolute top-4 right-4 p-2 text-stone-400 hover:text-stone-600 dark:hover:text-stone-300 transition-colors duration-300"
				>
					<X
						size={18}
						strokeWidth={1.5}
					/>
				</button>

				<div className="p-8 pt-12">
					{/* Header */}
					<div className="text-center mb-8">
						<div className="w-12 h-12 mx-auto mb-4 relative">
							<Image
								src="/logo/brandmark.png"
								alt="Galerique"
								fill
								className="object-contain"
							/>
						</div>
						<h2 className="font-[var(--font-bricolage)] text-2xl font-bold text-stone-900 dark:text-stone-100">
							{mode === "login" ? "Welcome back" : "Join Galerique"}
						</h2>
						<p className="mt-2 text-sm text-stone-500 dark:text-stone-400">
							{mode === "login"
								? "Sign in to continue your journey"
								: "Discover and collect extraordinary art"}
						</p>
					</div>

					{error && (
						<div className="mb-6 p-3 bg-red-50 dark:bg-red-900/10 border border-red-100 dark:border-red-900/30 rounded-lg text-red-600 dark:text-red-400 text-sm text-center">
							{error}
						</div>
					)}

					<form
						onSubmit={handleSubmit}
						className="space-y-4"
					>
						{mode === "signup" ? (
							<>
								<div>
									<label className="block text-xs font-medium text-stone-500 dark:text-stone-400 mb-2">
										Username
									</label>
									<input
										type="text"
										value={username}
										onChange={(e) => setUsername(e.target.value)}
										placeholder="Choose a username"
										className="w-full px-4 py-3 bg-stone-50 dark:bg-stone-800 border border-stone-200 dark:border-stone-700 rounded-lg text-sm text-stone-900 dark:text-stone-100 placeholder-stone-400 dark:placeholder-stone-600 focus:outline-none focus:border-stone-400 dark:focus:border-stone-600 transition-colors duration-300"
										required
									/>
								</div>
								<div>
									<label className="block text-xs font-medium text-stone-500 dark:text-stone-400 mb-2">
										Email
									</label>
									<input
										type="email"
										value={email}
										onChange={(e) => setEmail(e.target.value)}
										placeholder="you@example.com"
										className="w-full px-4 py-3 bg-stone-50 dark:bg-stone-800 border border-stone-200 dark:border-stone-700 rounded-lg text-sm text-stone-900 dark:text-stone-100 placeholder-stone-400 dark:placeholder-stone-600 focus:outline-none focus:border-stone-400 dark:focus:border-stone-600 transition-colors duration-300"
										required
									/>
								</div>
							</>
						) : (
							<div>
								<label className="block text-xs font-medium text-stone-500 dark:text-stone-400 mb-2">
									Email or Username
								</label>
								<input
									type="text"
									value={identifier}
									onChange={(e) => setIdentifier(e.target.value)}
									placeholder="Enter your email or username"
									className="w-full px-4 py-3 bg-stone-50 dark:bg-stone-800 border border-stone-200 dark:border-stone-700 rounded-lg text-sm text-stone-900 dark:text-stone-100 placeholder-stone-400 dark:placeholder-stone-600 focus:outline-none focus:border-stone-400 dark:focus:border-stone-600 transition-colors duration-300"
									required
								/>
							</div>
						)}

						<div>
							<label className="block text-xs font-medium text-stone-500 dark:text-stone-400 mb-2">
								Password
							</label>
							<div className="relative">
								<input
									type={showPassword ? "text" : "password"}
									value={password}
									onChange={(e) => setPassword(e.target.value)}
									placeholder="••••••••"
									className="w-full px-4 py-3 bg-stone-50 dark:bg-stone-800 border border-stone-200 dark:border-stone-700 rounded-lg text-sm text-stone-900 dark:text-stone-100 placeholder-stone-400 dark:placeholder-stone-600 focus:outline-none focus:border-stone-400 dark:focus:border-stone-600 transition-colors duration-300 pr-12"
									required
								/>
								<button
									type="button"
									onClick={() => setShowPassword(!showPassword)}
									className="absolute right-3 top-1/2 -translate-y-1/2 text-stone-400 hover:text-stone-600 dark:hover:text-stone-300 transition-colors"
								>
									{showPassword ? (
										<EyeOff
											size={16}
											strokeWidth={1.5}
										/>
									) : (
										<Eye
											size={16}
											strokeWidth={1.5}
										/>
									)}
								</button>
							</div>
						</div>

						{mode === "login" && (
							<div className="flex items-center gap-2">
								<input
									type="checkbox"
									id="remember"
									checked={remember}
									onChange={(e) => setRemember(e.target.checked)}
									className="w-3.5 h-3.5 rounded border-stone-300 dark:border-stone-600 text-stone-900 dark:text-stone-100 focus:ring-stone-500"
								/>
								<label
									htmlFor="remember"
									className="text-xs text-stone-500 dark:text-stone-400"
								>
									Remember me
								</label>
							</div>
						)}

						<button
							type="submit"
							disabled={loading}
							className="w-full py-3 bg-stone-900 dark:bg-stone-100 text-white dark:text-stone-900 rounded-full text-sm font-medium hover:bg-stone-800 dark:hover:bg-stone-200 transition-colors duration-300 disabled:opacity-50 disabled:cursor-not-allowed"
						>
							{loading ? "Loading..." : mode === "login" ? "Sign in" : "Create account"}
						</button>
					</form>

					<p className="mt-6 text-center text-xs text-stone-500 dark:text-stone-400">
						{mode === "login" ? (
							<>
								Don&apos;t have an account?{" "}
								<button
									onClick={() => {
										setMode("signup");
										setError("");
									}}
									className="text-stone-900 dark:text-stone-100 hover:underline font-medium"
								>
									Sign up
								</button>
							</>
						) : (
							<>
								Already have an account?{" "}
								<button
									onClick={() => {
										setMode("login");
										setError("");
									}}
									className="text-stone-900 dark:text-stone-100 hover:underline font-medium"
								>
									Sign in
								</button>
							</>
						)}
					</p>
				</div>
			</div>
		</div>
	);
}
