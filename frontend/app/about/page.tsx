"use client";

import React, { useEffect, useRef } from "react";
import Image from "next/image";
import Link from "next/link";
import { Shield, Heart, ArrowRight, Palette, Globe, Award } from "lucide-react";

export default function AboutPage() {
	const observerRef = useRef<IntersectionObserver | null>(null);

	useEffect(() => {
		observerRef.current = new IntersectionObserver(
			(entries) => {
				entries.forEach((entry) => {
					if (entry.isIntersecting) {
						entry.target.classList.add("animate-fade-in-up");
						entry.target.classList.remove("opacity-0", "translate-y-8");
					}
				});
			},
			{ threshold: 0.1, rootMargin: "0px 0px -50px 0px" },
		);

		const elements = document.querySelectorAll(".scroll-animate");
		elements.forEach((el) => observerRef.current?.observe(el));

		return () => observerRef.current?.disconnect();
	}, []);

	return (
		<div className="min-h-screen">
			{/* Hero Section */}
			<section className="relative min-h-[70vh] flex items-center justify-center bg-stone-50 dark:bg-stone-950">
				<div className="relative max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 text-center py-32">
					<div className="animate-fade-in">
						<p className="tracking-editorial text-stone-400 mb-8">Est. 2024</p>
					</div>

					<h1 className="font-[var(--font-bricolage)] text-5xl sm:text-6xl lg:text-7xl font-extrabold text-stone-900 dark:text-white mb-6 animate-fade-in stagger-1">
						The Gallery of
						<br />
						Tomorrow
					</h1>

					<div className="editorial-line mx-auto mb-8" />

					<p className="text-lg sm:text-xl text-stone-500 dark:text-stone-400 max-w-2xl mx-auto mb-10 animate-fade-in stagger-2">
						Galerique is where extraordinary art meets passionate collectors. We&apos;re building the
						world&apos;s most prestigious digital art gallery, one masterpiece at a time.
					</p>

					<div className="flex flex-wrap items-center justify-center gap-4 animate-fade-in stagger-3">
						<Link
							href="/"
							className="inline-flex items-center gap-2 px-8 py-4 bg-stone-900 dark:bg-white text-white dark:text-stone-900 rounded-full font-medium hover:bg-stone-800 dark:hover:bg-stone-100 transition-all duration-300"
						>
							Explore Gallery
							<ArrowRight
								size={18}
								strokeWidth={1.5}
							/>
						</Link>
						<Link
							href="/upload"
							className="inline-flex items-center gap-2 px-8 py-4 bg-white dark:bg-stone-900 text-stone-900 dark:text-white rounded-full font-medium hover:bg-stone-100 dark:hover:bg-stone-800 transition-all duration-300 border border-stone-200 dark:border-stone-800"
						>
							<Palette
								size={18}
								strokeWidth={1.5}
							/>
							Submit Your Art
						</Link>
					</div>
				</div>
			</section>

			{/* Stats Section */}
			<section className="py-20 bg-white dark:bg-stone-900 border-y border-stone-200 dark:border-stone-800">
				<div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
					<div className="grid grid-cols-2 md:grid-cols-4 gap-8">
						{[
							{ value: "10K+", label: "Artworks" },
							{ value: "2.5K+", label: "Artists" },
							{ value: "50K+", label: "Collectors" },
							{ value: "€2M+", label: "Artist Earnings" },
						].map((stat, i) => (
							<div
								key={i}
								className="scroll-animate opacity-0 translate-y-8 text-center"
								style={{ transitionDelay: `${i * 100}ms` }}
							>
								<div className="font-[var(--font-bricolage)] text-4xl sm:text-5xl font-extrabold text-stone-900 dark:text-white mb-2">
									{stat.value}
								</div>
								<div className="text-stone-500 dark:text-stone-400 text-sm uppercase tracking-wider">
									{stat.label}
								</div>
							</div>
						))}
					</div>
				</div>
			</section>

			{/* Mission Section */}
			<section className="py-24 sm:py-32">
				<div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
					<div className="grid lg:grid-cols-2 gap-16 items-center">
						<div className="scroll-animate opacity-0 translate-y-8">
							<p className="tracking-editorial text-stone-400 mb-4">Our Mission</p>
							<h2 className="font-[var(--font-bricolage)] text-3xl sm:text-4xl font-extrabold text-stone-900 dark:text-white mb-6">
								Art Deserves Better
							</h2>
							<div className="editorial-line mb-8" />
							<p className="text-lg text-stone-500 dark:text-stone-400 mb-6 leading-relaxed">
								Art has always been a mirror of human creativity and emotion. In the digital age, we
								believe extraordinary art deserves an equally extraordinary platform.
							</p>
							<p className="text-lg text-stone-500 dark:text-stone-400 mb-8 leading-relaxed">
								Galerique was founded with a singular vision: to create a space where digital art is
								celebrated, artists are fairly compensated, and collectors can discover works that move
								their souls.
							</p>
							<div className="flex items-center gap-4">
								<div className="relative w-12 h-12">
									<Image
										src="/logo/brandmark.png"
										alt="Galerique"
										fill
										className="object-contain"
									/>
								</div>
								<div>
									<div className="font-medium text-stone-900 dark:text-white">Founded in Belgium</div>
									<div className="text-sm text-stone-500 dark:text-stone-400">
										A UCLL Cloud Native Project
									</div>
								</div>
							</div>
						</div>
						<div className="scroll-animate opacity-0 translate-y-8 relative aspect-[4/3] rounded-2xl overflow-hidden bg-stone-100 dark:bg-stone-800">
							<div className="absolute inset-0 flex items-center justify-center">
								<div className="relative w-32 h-32">
									<Image
										src="/logo/brandmark.png"
										alt="Galerique Logo"
										fill
										className="object-contain"
									/>
								</div>
							</div>
						</div>
					</div>
				</div>
			</section>

			{/* Values Section */}
			<section className="py-24 sm:py-32 bg-stone-50 dark:bg-stone-950">
				<div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
					<div className="text-center mb-16 scroll-animate opacity-0 translate-y-8">
						<p className="tracking-editorial text-stone-400 mb-4">Principles</p>
						<h2 className="font-[var(--font-bricolage)] text-3xl sm:text-4xl font-extrabold text-stone-900 dark:text-white mb-4">
							Our Values
						</h2>
						<div className="editorial-line mx-auto mb-6" />
						<p className="text-stone-500 dark:text-stone-400 max-w-2xl mx-auto">
							The principles that guide everything we do at Galerique.
						</p>
					</div>

					<div className="grid md:grid-cols-2 lg:grid-cols-4 gap-8">
						{[
							{
								icon: (
									<Heart
										className="text-stone-600 dark:text-stone-300"
										size={24}
										strokeWidth={1.5}
									/>
								),
								title: "Artist First",
								description:
									"We put artists at the center of everything. Fair compensation, creative freedom, and global exposure.",
							},
							{
								icon: (
									<Shield
										className="text-stone-600 dark:text-stone-300"
										size={24}
										strokeWidth={1.5}
									/>
								),
								title: "Trust & Safety",
								description:
									"Every artwork is verified. Every transaction is secure. Every collector is protected.",
							},
							{
								icon: (
									<Globe
										className="text-stone-600 dark:text-stone-300"
										size={24}
										strokeWidth={1.5}
									/>
								),
								title: "Global Community",
								description:
									"Connecting artists and collectors from every corner of the world in one shared space.",
							},
							{
								icon: (
									<Award
										className="text-stone-600 dark:text-stone-300"
										size={24}
										strokeWidth={1.5}
									/>
								),
								title: "Quality Curated",
								description:
									"We celebrate excellence. Our trending algorithm surfaces the most exceptional works.",
							},
						].map((value, i) => (
							<div
								key={i}
								className="scroll-animate opacity-0 translate-y-8 p-8 bg-white dark:bg-stone-900 rounded-2xl border border-stone-200 dark:border-stone-800 hover:border-stone-300 dark:hover:border-stone-700 transition-all duration-300"
								style={{ transitionDelay: `${i * 100}ms` }}
							>
								<div className="w-14 h-14 rounded-xl bg-stone-100 dark:bg-stone-800 flex items-center justify-center mb-6">
									{value.icon}
								</div>
								<h3 className="font-semibold text-lg text-stone-900 dark:text-white mb-3">
									{value.title}
								</h3>
								<p className="text-stone-500 dark:text-stone-400 text-sm leading-relaxed">
									{value.description}
								</p>
							</div>
						))}
					</div>
				</div>
			</section>

			{/* For Artists Section */}
			<section
				id="artists"
				className="py-24 sm:py-32"
			>
				<div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
					<div className="grid lg:grid-cols-2 gap-16 items-center">
						<div className="order-2 lg:order-1 scroll-animate opacity-0 translate-y-8 relative aspect-[4/3] rounded-2xl overflow-hidden bg-stone-100 dark:bg-stone-800">
							<div className="absolute inset-0 flex items-center justify-center">
								<Palette
									className="w-24 h-24 text-stone-300 dark:text-stone-600"
									strokeWidth={1}
								/>
							</div>
						</div>
						<div className="order-1 lg:order-2 scroll-animate opacity-0 translate-y-8">
							<p className="tracking-editorial text-stone-400 mb-4">For Artists</p>
							<h2 className="font-[var(--font-bricolage)] text-3xl sm:text-4xl font-extrabold text-stone-900 dark:text-white mb-6">
								Your Art Deserves a Stage
							</h2>
							<div className="editorial-line mb-8" />
							<ul className="space-y-4 mb-8">
								{[
									"Upload unlimited artworks with no listing fees",
									"Set your own prices and receive instant payments",
									"Build your audience with powerful profile tools",
									"Get discovered through our intelligent trending system",
								].map((item, i) => (
									<li
										key={i}
										className="flex items-start gap-3 text-stone-500 dark:text-stone-400"
									>
										<div className="w-5 h-5 rounded-full bg-stone-200 dark:bg-stone-700 flex items-center justify-center flex-shrink-0 mt-0.5">
											<svg
												className="w-3 h-3 text-stone-600 dark:text-stone-300"
												fill="currentColor"
												viewBox="0 0 20 20"
											>
												<path
													fillRule="evenodd"
													d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
													clipRule="evenodd"
												/>
											</svg>
										</div>
										{item}
									</li>
								))}
							</ul>
							<Link
								href="/upload"
								className="inline-flex items-center gap-2 px-6 py-3 bg-stone-900 dark:bg-white text-white dark:text-stone-900 rounded-full font-medium hover:bg-stone-800 dark:hover:bg-stone-100 transition-all duration-300"
							>
								Start Uploading
								<ArrowRight
									size={16}
									strokeWidth={1.5}
								/>
							</Link>
						</div>
					</div>
				</div>
			</section>

			{/* Guidelines Section */}
			<section
				id="guidelines"
				className="py-24 sm:py-32 bg-stone-50 dark:bg-stone-950"
			>
				<div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8">
					<div className="text-center mb-12 scroll-animate opacity-0 translate-y-8">
						<p className="tracking-editorial text-stone-400 mb-4">Standards</p>
						<h2 className="font-[var(--font-bricolage)] text-3xl sm:text-4xl font-extrabold text-stone-900 dark:text-white mb-4">
							Community Guidelines
						</h2>
						<div className="editorial-line mx-auto mb-6" />
						<p className="text-stone-500 dark:text-stone-400">
							A few simple rules to keep Galerique exceptional for everyone.
						</p>
					</div>

					<div className="space-y-6 scroll-animate opacity-0 translate-y-8">
						{[
							{
								title: "Original Work Only",
								description:
									"All artwork must be your own original creation. No AI-generated content, stolen work, or unauthorized derivatives.",
							},
							{
								title: "Respect Copyright",
								description:
									"Do not upload content that infringes on the intellectual property rights of others.",
							},
							{
								title: "Keep it Appropriate",
								description:
									"While we celebrate artistic expression, explicit pornographic content is not permitted.",
							},
							{
								title: "Be Authentic",
								description:
									"Use real information in your profile. Impersonation of other artists is strictly prohibited.",
							},
							{
								title: "Support Fellow Artists",
								description:
									"Engage constructively with the community. Harassment or discriminatory behavior will result in account termination.",
							},
						].map((rule, i) => (
							<div
								key={i}
								className="p-6 bg-white dark:bg-stone-900 rounded-xl border border-stone-200 dark:border-stone-800"
							>
								<h3 className="font-semibold text-stone-900 dark:text-white mb-2">{rule.title}</h3>
								<p className="text-stone-500 dark:text-stone-400 text-sm">{rule.description}</p>
							</div>
						))}
					</div>
				</div>
			</section>

			{/* CTA Section */}
			<section className="py-24 sm:py-32">
				<div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 text-center scroll-animate opacity-0 translate-y-8">
					<h2 className="font-[var(--font-bricolage)] text-3xl sm:text-4xl font-extrabold text-stone-900 dark:text-white mb-6">
						Ready to Begin?
					</h2>
					<div className="editorial-line mx-auto mb-8" />
					<p className="text-lg text-stone-500 dark:text-stone-400 mb-10 max-w-2xl mx-auto">
						Whether you&apos;re an artist looking to share your vision or a collector seeking your next
						masterpiece, Galerique awaits.
					</p>
					<div className="flex flex-wrap items-center justify-center gap-4">
						<Link
							href="/"
							className="inline-flex items-center gap-2 px-8 py-4 bg-stone-900 dark:bg-white text-white dark:text-stone-900 rounded-full font-medium hover:bg-stone-800 dark:hover:bg-stone-100 transition-all duration-300"
						>
							Explore the Gallery
							<ArrowRight
								size={18}
								strokeWidth={1.5}
							/>
						</Link>
					</div>
				</div>
			</section>
		</div>
	);
}
