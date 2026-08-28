"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { ArrowLeft, TrendingUp } from "lucide-react";
import { ArtworkSummaryDto } from "@/src/types";
import ArtworkService from "@/src/services/artwork.service";
import ArtworkGrid from "@/src/components/ArtworkGrid";

export default function TrendingPage() {
	const [artworks, setArtworks] = useState<ArtworkSummaryDto[]>([]);
	const [loading, setLoading] = useState(true);

	useEffect(() => {
		async function fetchTrending() {
			try {
				setLoading(true);
				const response = await ArtworkService.trending();
				if (response.ok) {
					const data = await response.json();
					setArtworks(data);
				}
			} catch (err) {
				console.error("Failed to fetch trending:", err);
			} finally {
				setLoading(false);
			}
		}
		fetchTrending();
	}, []);

	return (
		<div className="min-h-screen">
			<div className="max-w-[1400px] mx-auto px-6 lg:px-10 py-12">
				<Link
					href="/"
					className="inline-flex items-center gap-2 text-sm text-stone-500 hover:text-stone-900 dark:hover:text-stone-100 mb-10 transition-colors duration-300"
				>
					<ArrowLeft
						size={16}
						strokeWidth={1.5}
					/>
					Back
				</Link>

				{/* Header */}
				<div className="mb-16">
					<p className="tracking-editorial text-stone-400 dark:text-stone-600 mb-3">Exhibition</p>
					<h1 className="font-[var(--font-bricolage)] text-4xl sm:text-5xl font-bold text-stone-900 dark:text-stone-100 leading-[1.1] mb-4">
						Trending
					</h1>
					<p className="text-stone-500 dark:text-stone-400 text-base max-w-md">
						The most viewed artworks on Galerique right now.
					</p>
				</div>

				{/* Top 3 podium */}
				{!loading && artworks.length > 0 && (
					<div className="grid grid-cols-1 md:grid-cols-3 gap-6 lg:gap-8 mb-24">
						{artworks.slice(0, 3).map((artwork, index) => (
							<Link
								key={artwork.id}
								href={`/artwork/${artwork.id}`}
								className="group block"
							>
								<div className="relative overflow-hidden rounded-lg bg-stone-100 dark:bg-stone-900 aspect-[4/5]">
									<img
										src={artwork.thumbnailUrl || artwork.imageUrl || "/logo/brandmark_squared.png"}
										alt={artwork.title}
										className="w-full h-full object-cover transition-transform duration-700 ease-out group-hover:scale-[1.03]"
									/>
									<div className="absolute inset-0 bg-gradient-to-t from-black/30 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500" />
									{/* Rank */}
									<div className="absolute top-4 left-4 w-9 h-9 rounded-full bg-white/80 dark:bg-stone-900/80 backdrop-blur-sm flex items-center justify-center text-sm font-bold text-stone-700 dark:text-stone-300">
										{index + 1}
									</div>
								</div>
								<div className="mt-4">
									<h3 className="text-sm font-medium text-stone-800 dark:text-stone-200 group-hover:text-stone-500 transition-colors duration-300 line-clamp-1">
										{artwork.title}
									</h3>
									<p className="text-xs text-stone-400 dark:text-stone-500 mt-1">
										{artwork.creator?.username || "Unknown"} &middot;{" "}
										{artwork.views?.toLocaleString() || 0} views
									</p>
								</div>
							</Link>
						))}
					</div>
				)}

				{/* Rest */}
				{artworks.length > 3 && (
					<div className="border-t border-stone-200 dark:border-stone-800 pt-16">
						<p className="tracking-editorial text-stone-400 dark:text-stone-600 mb-3">More</p>
						<h2 className="font-[var(--font-bricolage)] text-2xl font-semibold text-stone-900 dark:text-stone-100 mb-10">
							Also Trending
						</h2>
						<ArtworkGrid
							artworks={artworks.slice(3)}
							loading={false}
						/>
					</div>
				)}

				{/* Empty */}
				{!loading && artworks.length === 0 && (
					<div className="text-center py-24">
						<TrendingUp
							size={32}
							strokeWidth={1}
							className="mx-auto mb-4 text-stone-300 dark:text-stone-700"
						/>
						<p className="text-stone-400 dark:text-stone-600 text-sm">No trending artworks yet</p>
					</div>
				)}

				{/* Loading */}
				{loading && (
					<div className="grid grid-cols-1 md:grid-cols-3 gap-6 lg:gap-8">
						{[...Array(3)].map((_, i) => (
							<div key={i}>
								<div className="aspect-[4/5] rounded-lg bg-stone-100 dark:bg-stone-900 animate-pulse" />
								<div className="mt-4 h-4 w-32 bg-stone-100 dark:bg-stone-900 rounded animate-pulse" />
								<div className="mt-2 h-3 w-20 bg-stone-100 dark:bg-stone-900 rounded animate-pulse" />
							</div>
						))}
					</div>
				)}
			</div>
		</div>
	);
}
