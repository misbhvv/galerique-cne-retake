"use client";
import Link from "next/link";
import Image from "next/image";
import React from "react";

export default function NotFound() {
	return (
		<div className="min-h-screen bg-white dark:bg-stone-950 relative overflow-hidden">
			<div className="relative flex flex-col items-center justify-center min-h-screen px-6">
				{/* Logo */}
				<div className="mb-12 animate-fade-in">
					<Link
						href="/"
						className="opacity-40 hover:opacity-70 transition-opacity duration-500"
					>
						<Image
							src="/logo/brandmark.png"
							alt="Galerique"
							width={48}
							height={48}
							className="dark:invert"
						/>
					</Link>
				</div>

				{/* 404 number - large typographic element */}
				<div className="animate-fade-in">
					<h1 className="font-[var(--font-bricolage)] text-[12rem] sm:text-[16rem] lg:text-[20rem] font-black leading-none tracking-tighter text-stone-100 dark:text-stone-900 select-none">
						404
					</h1>
				</div>

				{/* Content overlay */}
				<div className="absolute inset-0 flex flex-col items-center justify-center px-6">
					<div className="mt-8 text-center max-w-lg animate-fade-in stagger-1">
						<p className="tracking-editorial text-stone-400 dark:text-stone-500 mb-4">Page not found</p>
						<h2 className="font-[var(--font-bricolage)] text-3xl sm:text-4xl lg:text-5xl font-bold text-stone-900 dark:text-white mb-6">
							Lost in the gallery
						</h2>
						<div className="editorial-line mx-auto mb-6" />
						<p className="text-stone-500 dark:text-stone-400 text-lg leading-relaxed mb-10">
							The page you&apos;re looking for has been moved, removed, or perhaps never existed.
							Let&apos;s get you back to the art.
						</p>

						<div className="flex flex-col sm:flex-row items-center justify-center gap-4">
							<Link
								href="/"
								className="px-8 py-3.5 bg-stone-900 dark:bg-white text-white dark:text-stone-900 rounded-full text-sm font-medium hover:bg-stone-800 dark:hover:bg-stone-100 transition-all duration-300"
							>
								Back to Gallery
							</Link>
							<Link
								href="/about"
								className="px-8 py-3.5 text-stone-500 dark:text-stone-400 hover:text-stone-900 dark:hover:text-white text-sm font-medium transition-all duration-300"
							>
								About Galerique
							</Link>
						</div>
					</div>
				</div>

				{/* Decorative line */}
				<div className="absolute bottom-24 left-1/2 -translate-x-1/2 w-px h-16 bg-gradient-to-b from-stone-300 dark:from-stone-700 to-transparent animate-fade-in stagger-2" />

				{/* Bottom text */}
				<div className="absolute bottom-10 left-1/2 -translate-x-1/2 animate-fade-in stagger-3">
					<p className="text-xs text-stone-400 dark:text-stone-600 tracking-widest uppercase">
						Galerique &mdash; Digital Art Gallery
					</p>
				</div>
			</div>
		</div>
	);
}
