"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { ArrowLeft, ShoppingBag } from "lucide-react";
import { PurchaseDto } from "@/src/types";
import PurchaseService from "@/src/services/purchase.service";
import { useAuth } from "@/src/context/AuthContext";

export default function PurchasesPage() {
	const { user, loading: authLoading } = useAuth();
	const [purchases, setPurchases] = useState<PurchaseDto[]>([]);
	const [loading, setLoading] = useState(true);

	useEffect(() => {
		async function fetchPurchases() {
			if (!user) return;
			try {
				setLoading(true);
				const response = await PurchaseService.myPurchases();
				if (response.ok) {
					const data = await response.json();
					setPurchases(data);
				}
			} catch (err) {
				console.error("Failed to fetch purchases:", err);
			} finally {
				setLoading(false);
			}
		}

		if (user) {
			fetchPurchases();
		} else if (!authLoading) {
			setLoading(false);
		}
	}, [user, authLoading]);

	if (!authLoading && !user) {
		return (
			<div className="min-h-screen flex items-center justify-center">
				<div className="text-center">
					<h1 className="font-[var(--font-bricolage)] text-2xl font-semibold text-stone-900 dark:text-stone-100 mb-2">
						Sign in required
					</h1>
					<p className="text-stone-500 dark:text-stone-400 text-sm mb-6">
						You need to be signed in to view your purchases.
					</p>
					<Link
						href="/"
						className="text-sm text-stone-500 hover:text-stone-900 dark:hover:text-stone-100 transition-colors duration-300"
					>
						<ArrowLeft
							size={14}
							strokeWidth={1.5}
							className="inline mr-1"
						/>
						Back to gallery
					</Link>
				</div>
			</div>
		);
	}

	const totalSpent = purchases.reduce((sum, p) => sum + (p.purchasePrice || 0), 0);

	return (
		<div className="min-h-screen">
			<div className="max-w-3xl mx-auto px-6 lg:px-10 py-12">
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
				<div className="mb-10">
					<p className="tracking-editorial text-stone-400 dark:text-stone-600 mb-3">Collection</p>
					<h1 className="font-[var(--font-bricolage)] text-3xl sm:text-4xl font-bold text-stone-900 dark:text-stone-100 leading-[1.1]">
						My Purchases
					</h1>
				</div>

				{/* Stats */}
				{!loading && purchases.length > 0 && (
					<div className="flex items-center gap-6 mb-10 pb-6 border-b border-stone-200 dark:border-stone-800">
						<p className="text-sm text-stone-500 dark:text-stone-400">
							<strong className="text-stone-900 dark:text-stone-100">{purchases.length}</strong> artwork
							{purchases.length !== 1 ? "s" : ""} owned
						</p>
						<p className="text-sm text-stone-500 dark:text-stone-400">
							<strong className="text-stone-900 dark:text-stone-100">
								&euro;{totalSpent.toLocaleString()}
							</strong>{" "}
							total
						</p>
					</div>
				)}

				{/* Loading */}
				{loading && (
					<div className="space-y-4">
						{[...Array(4)].map((_, i) => (
							<div
								key={i}
								className="flex gap-4 p-4 border border-stone-100 dark:border-stone-900 rounded-lg"
							>
								<div className="w-20 h-20 bg-stone-100 dark:bg-stone-900 rounded-md animate-pulse shrink-0" />
								<div className="flex-1">
									<div className="h-4 w-40 bg-stone-100 dark:bg-stone-900 rounded animate-pulse mb-2" />
									<div className="h-3 w-28 bg-stone-100 dark:bg-stone-900 rounded animate-pulse mb-3" />
									<div className="h-3 w-20 bg-stone-100 dark:bg-stone-900 rounded animate-pulse" />
								</div>
							</div>
						))}
					</div>
				)}

				{/* Purchases list */}
				{!loading && purchases.length > 0 && (
					<div className="space-y-3">
						{purchases.map((purchase) => (
							<Link
								key={purchase.id}
								href={`/artwork/${purchase.artwork.id}`}
								className="flex gap-4 p-4 border border-stone-200 dark:border-stone-800 rounded-lg hover:border-stone-300 dark:hover:border-stone-700 hover:bg-stone-50/50 dark:hover:bg-stone-900/50 transition-all duration-300 group"
							>
								<div className="w-20 h-20 rounded-md overflow-hidden bg-stone-100 dark:bg-stone-900 shrink-0">
									<img
										src={
											purchase.artwork.thumbnailUrl ||
											purchase.artwork.imageUrl ||
											"/logo/brandmark_squared.png"
										}
										alt={purchase.artwork.title}
										className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
									/>
								</div>

								<div className="flex-1 min-w-0 py-0.5">
									<h3 className="font-medium text-stone-900 dark:text-stone-100 text-sm truncate mb-1">
										{purchase.artwork.title}
									</h3>
									<p className="text-xs text-stone-500 dark:text-stone-400 mb-2">
										by {purchase.artwork.creator.username}
									</p>
									<div className="flex items-center gap-3 text-xs text-stone-400 dark:text-stone-600">
										<span className="font-medium text-stone-900 dark:text-stone-100">
											&euro;{purchase.purchasePrice?.toLocaleString() || "0"}
										</span>
										{purchase.purchaseDate && (
											<span>
												{new Date(purchase.purchaseDate).toLocaleDateString("en-US", {
													month: "short",
													day: "numeric",
													year: "numeric",
												})}
											</span>
										)}
									</div>
								</div>
							</Link>
						))}
					</div>
				)}

				{/* Empty state */}
				{!loading && purchases.length === 0 && (
					<div className="text-center py-20">
						<div className="w-14 h-14 mx-auto mb-4 rounded-full bg-stone-100 dark:bg-stone-900 flex items-center justify-center">
							<ShoppingBag
								size={20}
								strokeWidth={1.5}
								className="text-stone-400 dark:text-stone-600"
							/>
						</div>
						<p className="text-stone-500 dark:text-stone-400 text-sm mb-4">No purchases yet</p>
						<Link
							href="/"
							className="inline-flex items-center gap-2 px-6 py-2.5 bg-stone-900 dark:bg-stone-100 text-white dark:text-stone-900 rounded-full text-sm font-medium hover:bg-stone-800 dark:hover:bg-stone-200 transition-colors duration-300"
						>
							Explore artworks
						</Link>
					</div>
				)}
			</div>
		</div>
	);
}
