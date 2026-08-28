"use client";

import React, { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { ArtworkSummaryDto } from "../types";
import { Heart, Eye } from "lucide-react";

interface ArtworkCardProps {
	artwork: ArtworkSummaryDto;
	onLike?: (id: string) => void;
	onTagSelect?: (tag: string) => void;
	priority?: boolean;
}

export default function ArtworkCard({ artwork, onLike, onTagSelect, priority }: ArtworkCardProps) {
	const router = useRouter();
	const [imageLoaded, setImageLoaded] = useState(false);
	const [imageSrc, setImageSrc] = useState(artwork.thumbnailUrl || artwork.imageUrl || "/logo/brandmark_squared.png");

	const formatPrice = (price: number) =>
		new Intl.NumberFormat("en-US", { style: "currency", currency: "EUR" }).format(price);

	const initials = (artwork.creator?.username || "?")
		.split(" ")
		.map((n: string) => n[0])
		.join("")
		.toUpperCase()
		.slice(0, 2);

	return (
		<div className="group">
			<Link href={`/artwork/${artwork.id}`}>
				<div className="relative overflow-hidden rounded-lg bg-gradient-to-br from-stone-100 via-stone-50 to-stone-200 dark:from-stone-900 dark:via-stone-950 dark:to-stone-800">
					{/* Skeleton */}
					{!imageLoaded && <div className="absolute inset-0 bg-stone-200 dark:bg-stone-800 animate-pulse" />}

					{/* Image */}
					<img
						src={imageSrc}
						alt={artwork.title}
						className={`w-full h-auto object-cover transition-transform duration-700 ease-out group-hover:scale-[1.03] ${
							imageLoaded ? "opacity-100" : "opacity-0"
						}`}
						onLoad={() => setImageLoaded(true)}
						onError={() => {
							setImageSrc("/logo/brandmark_squared.png");
							setImageLoaded(true);
						}}
						loading={priority ? "eager" : "lazy"}
					/>

					{/* Hover overlay - subtle gradient */}
					<div className="absolute inset-0 bg-gradient-to-t from-black/50 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500" />

					{/* Hover info at bottom */}
					<div className="absolute bottom-0 left-0 right-0 p-5 translate-y-2 opacity-0 group-hover:translate-y-0 group-hover:opacity-100 transition-all duration-500">
						<p className="text-white/90 text-sm font-medium">{formatPrice(artwork.price)}</p>
					</div>

					{/* Like button */}
					<button
						title="Like artwork"
						onClick={(e) => {
							e.preventDefault();
							e.stopPropagation();
							onLike?.(artwork.id);
						}}
						className="absolute top-3 right-3 p-2 bg-white/80 dark:bg-stone-900/80 backdrop-blur-sm rounded-full opacity-0 group-hover:opacity-100 transition-all duration-300 hover:scale-110 hover:bg-white dark:hover:bg-stone-800"
					>
						<Heart
							size={15}
							className="text-stone-600 dark:text-stone-300 hover:text-red-500 transition-colors"
							strokeWidth={1.5}
						/>
					</button>
				</div>
			</Link>

			{/* Card info - minimal */}
			<div className="mt-3 space-y-1">
				<h3 className="text-sm font-medium text-stone-800 dark:text-stone-200 line-clamp-1 group-hover:text-stone-600 dark:group-hover:text-stone-400 transition-colors duration-300">
					{artwork.title}
				</h3>
				{typeof artwork.searchScore === "number" && artwork.searchScore > 0 && (
					<p className="text-[10px] uppercase tracking-wide text-stone-400 dark:text-stone-500">
						Relevance {artwork.searchScore}
					</p>
				)}
				{artwork.tags && artwork.tags.length > 0 && (
					<div className="flex flex-wrap gap-1.5">
						{artwork.tags.slice(0, 2).map((tag) => (
							<button
								key={tag}
								type="button"
								onClick={(e) => {
									e.stopPropagation();
									e.preventDefault();
									if (onTagSelect) {
										onTagSelect(tag);
									} else {
										router.push(`/?tag=${encodeURIComponent(tag)}`);
									}
								}}
								className="px-2 py-0.5 rounded-full border border-stone-200 dark:border-stone-700 text-[10px] text-stone-500 dark:text-stone-400 hover:border-stone-400 dark:hover:border-stone-500 hover:text-stone-800 dark:hover:text-stone-200 transition-colors cursor-pointer"
							>
								{tag}
							</button>
						))}
					</div>
				)}
				<div className="flex items-center justify-between">
					<div className="flex items-center gap-2">
						<div className="w-5 h-5 rounded-full bg-stone-300 dark:bg-stone-700 flex items-center justify-center text-[8px] font-semibold text-stone-600 dark:text-stone-300">
							{initials}
						</div>
						<div className="flex flex-col">
							<span className="text-xs text-stone-500 dark:text-stone-500 truncate">
								{artwork.creator?.username || "Unknown"}
							</span>
							{artwork.year && (
								<span className="text-[10px] text-stone-400 dark:text-stone-600">{artwork.year}</span>
							)}
						</div>
					</div>
					<div className="flex items-center gap-1 text-xs text-stone-400 dark:text-stone-600">
						<Eye
							size={11}
							strokeWidth={1.5}
						/>
						{artwork.views ?? 0}
					</div>
				</div>
			</div>
		</div>
	);
}
