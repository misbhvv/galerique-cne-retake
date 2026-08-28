"use client";

import React, { useState, useEffect, useRef, useCallback } from "react";
import { Search, X, TrendingUp } from "lucide-react";
import { useRouter } from "next/navigation";
import { ArtworkSummaryDto } from "../types";
import ArtworkService from "../services/artwork.service";

interface SearchModalProps {
	isOpen: boolean;
	initialQuery?: string;
	onClose: () => void;
}

export default function SearchModal({ isOpen, initialQuery = "", onClose }: SearchModalProps) {
	const [query, setQuery] = useState("");
	const [results, setResults] = useState<ArtworkSummaryDto[]>([]);
	const [trending, setTrending] = useState<ArtworkSummaryDto[]>([]);
	const [loading, setLoading] = useState(false);
	const inputRef = useRef<HTMLInputElement>(null);
	const router = useRouter();

	useEffect(() => {
		if (isOpen) {
			setQuery(initialQuery);
			ArtworkService.trending()
				.then((res) => res.json())
				.then((data) => setTrending(data.slice(0, 5)))
				.catch(() => {});
			inputRef.current?.focus();
		}
	}, [isOpen, initialQuery]);

	const searchArtworks = useCallback(async (q: string) => {
		if (!q.trim()) {
			setResults([]);
			return;
		}
		setLoading(true);
		try {
			const res = await ArtworkService.search(q);
			const data = await res.json();
			setResults(data.slice(0, 8));
		} catch {
			setResults([]);
		} finally {
			setLoading(false);
		}
	}, []);

	useEffect(() => {
		const timer = setTimeout(() => {
			searchArtworks(query);
		}, 300);
		return () => clearTimeout(timer);
	}, [query, searchArtworks]);

	useEffect(() => {
		const handleKeyDown = (e: KeyboardEvent) => {
			if ((e.metaKey || e.ctrlKey) && e.key === "k") {
				e.preventDefault();
				if (isOpen) onClose();
			}
			if (e.key === "Escape" && isOpen) onClose();
		};
		window.addEventListener("keydown", handleKeyDown);
		return () => window.removeEventListener("keydown", handleKeyDown);
	}, [isOpen, onClose]);

	const navigateToArtwork = (id: string) => {
		router.push(`/artwork/${id}`);
		onClose();
		setQuery("");
	};

	if (!isOpen) return null;

	const displayResults = query.trim() ? results : trending;
	const showTrendingLabel = !query.trim() && trending.length > 0;

	return (
		<div className="fixed inset-0 z-[100] pt-20 px-4">
			<div
				className="absolute inset-0 bg-black/40 backdrop-blur-sm"
				onClick={onClose}
			/>

			<div className="relative max-w-xl mx-auto bg-white dark:bg-stone-900 rounded-xl shadow-2xl overflow-hidden border border-stone-200 dark:border-stone-800">
				{/* Search input */}
				<div className="flex items-center gap-3 px-5 py-4 border-b border-stone-200 dark:border-stone-800">
					<Search
						size={18}
						strokeWidth={1.5}
						className="text-stone-400 dark:text-stone-600"
					/>
					<input
						ref={inputRef}
						type="text"
						value={query}
						onChange={(e) => setQuery(e.target.value)}
						placeholder="Search artworks, artists..."
						className="flex-1 bg-transparent text-base text-stone-900 dark:text-stone-100 placeholder-stone-400 dark:placeholder-stone-600 focus:outline-none"
					/>
					{query && (
						<button
							onClick={() => setQuery("")}
							className="p-1 text-stone-400 hover:text-stone-600 dark:hover:text-stone-300 transition-colors"
						>
							<X
								size={16}
								strokeWidth={1.5}
							/>
						</button>
					)}
					<kbd className="hidden sm:block text-[10px] bg-stone-100 dark:bg-stone-800 text-stone-400 dark:text-stone-600 px-1.5 py-0.5 rounded">
						ESC
					</kbd>
				</div>

				{/* Results */}
				<div className="max-h-[380px] overflow-y-auto">
					{showTrendingLabel && (
						<div className="flex items-center gap-2 px-5 py-3 text-xs tracking-editorial text-stone-400 dark:text-stone-600">
							<TrendingUp
								size={12}
								strokeWidth={1.5}
							/>
							Trending
						</div>
					)}

					{loading && (
						<div className="px-5 py-8 text-center text-sm text-stone-400 dark:text-stone-600">
							Searching...
						</div>
					)}

					{!loading && displayResults.length === 0 && query.trim() && (
						<div className="px-5 py-8 text-center text-sm text-stone-400 dark:text-stone-600">
							No results for &quot;{query}&quot;
						</div>
					)}

					{!loading &&
						displayResults.map((artwork) => (
							<button
								key={artwork.id}
								onClick={() => navigateToArtwork(artwork.id)}
								className="w-full flex items-center gap-4 px-5 py-3 hover:bg-stone-50 dark:hover:bg-stone-800/50 transition-colors duration-200 text-left"
							>
								<img
									src={artwork.imageUrl || "/placeholder.jpg"}
									alt={artwork.title}
									className="w-11 h-11 rounded-md object-cover bg-stone-100 dark:bg-stone-800"
								/>
								<div className="flex-1 min-w-0">
									<p className="text-sm font-medium text-stone-900 dark:text-stone-100 truncate">
										{artwork.title}
									</p>
									<p className="text-xs text-stone-500 dark:text-stone-400">
										{artwork.creator?.username || "Unknown"}
									</p>
								</div>
								<span className="text-xs font-medium text-stone-500 dark:text-stone-400">
									&euro;{artwork.price.toFixed(2)}
								</span>
							</button>
						))}
				</div>

				{/* Footer */}
				<div className="px-5 py-2.5 border-t border-stone-200 dark:border-stone-800 flex items-center justify-between text-[10px] text-stone-400 dark:text-stone-600">
					<span>Type to search</span>
					<span className="flex items-center gap-1">
						<kbd className="bg-stone-100 dark:bg-stone-800 px-1.5 py-0.5 rounded">&crarr;</kbd>
						to select
					</span>
				</div>
			</div>
		</div>
	);
}
