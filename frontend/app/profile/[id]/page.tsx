"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import { ArrowLeft, Image as ImageIcon } from "lucide-react";
import { AccountDto, ArtworkSummaryDto } from "@/src/types";
import AccountService from "@/src/services/account.service";
import ArtworkGrid from "@/src/components/ArtworkGrid";
import { useAuth } from "@/src/context/AuthContext";

export default function ProfilePage() {
	const params = useParams();
	const id = params.id as string;
	const { user } = useAuth();

	const [account, setAccount] = useState<AccountDto | null>(null);
	const [artworks, setArtworks] = useState<ArtworkSummaryDto[]>([]);
	const [loading, setLoading] = useState(true);
	const [error, setError] = useState<string | null>(null);

	const isOwn = user?.id === String(id);

	useEffect(() => {
		async function fetchProfile() {
			try {
				setLoading(true);
				const [accountResponse, artworksResponse] = await Promise.all([
					AccountService.getById(id),
					AccountService.getArtworks(id),
				]);
				if (!accountResponse.ok || !artworksResponse.ok) {
					throw new Error("Failed to load profile");
				}
				const accountData = await accountResponse.json();
				const artworksData = await artworksResponse.json();
				setAccount(accountData);
				setArtworks(artworksData);
			} catch (err) {
				console.error("Failed to fetch profile:", err);
				setError("Profile not found");
			} finally {
				setLoading(false);
			}
		}

		if (id) {
			fetchProfile();
		}
	}, [id]);

	if (loading) {
		return (
			<div className="min-h-screen">
				<div className="max-w-[1400px] mx-auto px-6 lg:px-10 py-12">
					<div className="flex items-center gap-8 mb-12">
						<div className="w-24 h-24 sm:w-28 sm:h-28 rounded-full bg-stone-100 dark:bg-stone-900 animate-pulse" />
						<div className="flex-1">
							<div className="h-7 w-48 bg-stone-100 dark:bg-stone-900 rounded animate-pulse mb-3" />
							<div className="h-4 w-28 bg-stone-100 dark:bg-stone-900 rounded animate-pulse" />
						</div>
					</div>
					<div className="masonry-grid">
						{[...Array(6)].map((_, i) => (
							<div
								key={i}
								className="mb-4 rounded-md bg-stone-100 dark:bg-stone-900 animate-pulse"
								style={{ height: `${200 + (i % 3) * 80}px` }}
							/>
						))}
					</div>
				</div>
			</div>
		);
	}

	if (error || !account) {
		return (
			<div className="min-h-screen flex items-center justify-center">
				<div className="text-center">
					<h1 className="font-[var(--font-bricolage)] text-2xl font-semibold text-stone-900 dark:text-stone-100 mb-2">
						Profile not found
					</h1>
					<p className="text-stone-500 dark:text-stone-400 text-sm mb-6">
						This profile doesn&apos;t exist or has been removed.
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

	const initial = account.username.charAt(0).toUpperCase();

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

				{/* Profile header */}
				<div className="flex flex-col sm:flex-row items-center sm:items-start gap-6 sm:gap-8 mb-12">
					<div className="w-24 h-24 sm:w-28 sm:h-28 rounded-full bg-stone-900 dark:bg-stone-100 flex items-center justify-center text-white dark:text-stone-900 text-2xl sm:text-3xl font-semibold flex-shrink-0">
						{initial}
					</div>

					<div className="flex-1 text-center sm:text-left">
						<div className="flex flex-col sm:flex-row sm:items-center gap-3 mb-2">
							<h1 className="font-[var(--font-bricolage)] text-2xl sm:text-3xl font-bold text-stone-900 dark:text-stone-100">
								{account.username}
							</h1>
							{isOwn && (
								<span className="inline-flex items-center px-3 py-1 rounded-full text-[11px] font-medium bg-stone-100 dark:bg-stone-800 text-stone-500 dark:text-stone-400 self-center sm:self-auto">
									You
								</span>
							)}
						</div>

						{account.email && (
							<p className="text-sm text-stone-500 dark:text-stone-400 mb-3">{account.email}</p>
						)}

						<p className="text-sm text-stone-500 dark:text-stone-400">
							<strong className="text-stone-900 dark:text-stone-100">{artworks.length}</strong> artwork
							{artworks.length !== 1 ? "s" : ""}
						</p>
					</div>

					{isOwn && (
						<Link
							href="/settings"
							className="px-5 py-2 border border-stone-200 dark:border-stone-800 rounded-full text-sm font-medium text-stone-700 dark:text-stone-300 hover:bg-stone-50 dark:hover:bg-stone-900 transition-colors duration-300"
						>
							Edit Profile
						</Link>
					)}
				</div>

				<div className="editorial-line mb-8" />

				{/* Artworks */}
				<div className="mb-6">
					<p className="tracking-editorial text-stone-400 dark:text-stone-600">Works</p>
				</div>

				{artworks.length === 0 ? (
					<div className="text-center py-20">
						<div className="w-14 h-14 mx-auto mb-4 rounded-full bg-stone-100 dark:bg-stone-900 flex items-center justify-center">
							<ImageIcon
								size={20}
								strokeWidth={1.5}
								className="text-stone-400 dark:text-stone-600"
							/>
						</div>
						<p className="text-stone-500 dark:text-stone-400 text-sm mb-3">No artworks yet</p>
						{isOwn && (
							<Link
								href="/upload"
								className="text-sm font-medium text-stone-900 dark:text-stone-100 hover:underline"
							>
								Upload your first artwork
							</Link>
						)}
					</div>
				) : (
					<ArtworkGrid
						artworks={artworks}
						loading={false}
					/>
				)}
			</div>
		</div>
	);
}
