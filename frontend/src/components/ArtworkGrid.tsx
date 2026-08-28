"use client";

import React from "react";
import { ArtworkSummaryDto } from "../types";
import ArtworkCard from "./ArtworkCard";
import { ImageIcon } from "lucide-react";

interface ArtworkGridProps {
	artworks: ArtworkSummaryDto[];
	onLike?: (id: string) => void;
	onTagSelect?: (tag: string) => void;
	loading?: boolean;
}

export default function ArtworkGrid({ artworks, onLike, onTagSelect, loading }: ArtworkGridProps) {
	if (loading) {
		return (
			<div className="masonry-grid">
				{[...Array(6)].map((_, i) => (
					<div
						key={i}
						className="animate-pulse"
					>
						<div
							className="rounded-lg bg-stone-200 dark:bg-stone-800"
							style={{ height: `${220 + (i % 3) * 80}px` }}
						/>
						<div className="mt-3 space-y-2">
							<div className="h-4 w-3/4 rounded bg-stone-200 dark:bg-stone-800" />
							<div className="flex items-center gap-2">
								<div className="w-5 h-5 rounded-full bg-stone-200 dark:bg-stone-800" />
								<div className="w-16 h-3 rounded bg-stone-200 dark:bg-stone-800" />
							</div>
						</div>
					</div>
				))}
			</div>
		);
	}

	if (!artworks.length) {
		return (
			<div className="flex flex-col items-center justify-center py-24 text-center">
				<div className="w-16 h-16 mb-6 rounded-full bg-stone-100 dark:bg-stone-900 flex items-center justify-center">
					<ImageIcon
						size={24}
						className="text-stone-400 dark:text-stone-600"
						strokeWidth={1.5}
					/>
				</div>
				<h3 className="text-lg font-medium text-stone-800 dark:text-stone-200 mb-2">No artworks yet</h3>
				<p className="text-stone-500 dark:text-stone-500 max-w-sm text-sm leading-relaxed">
					The gallery is waiting for its first masterpiece. Be the first to share your work.
				</p>
			</div>
		);
	}

	return (
		<div className="masonry-grid">
			{artworks.map((artwork, i) => (
				<ArtworkCard
					key={artwork.id}
					artwork={artwork}
					onLike={onLike}
					onTagSelect={onTagSelect}
					priority={i < 4}
				/>
			))}
		</div>
	);
}
