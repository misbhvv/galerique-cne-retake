import Link from "next/link";

export default function TermsOfService() {
	return (
		<div className="min-h-screen bg-white dark:bg-zinc-950">
			{/* Hero */}
			<section className="relative overflow-hidden">
				<div className="absolute inset-0 pointer-events-none">
					<div className="absolute top-0 left-1/2 -translate-x-1/2 w-[600px] h-[600px] bg-gradient-to-b from-zinc-100 dark:from-zinc-900/50 to-transparent rounded-full blur-3xl opacity-50" />
				</div>
				<div className="relative max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 pt-32 pb-16">
					<p className="text-sm uppercase tracking-[0.3em] text-zinc-400 dark:text-zinc-600 mb-4 font-medium animate-fade-in">
						Legal
					</p>
					<h1 className="font-[var(--font-bricolage)] text-4xl sm:text-5xl font-bold text-zinc-900 dark:text-white animate-fade-in stagger-1">
						Terms of Service
					</h1>
					<p className="mt-4 text-zinc-500 dark:text-zinc-400 text-lg animate-fade-in stagger-2">
						Last updated: January 2025
					</p>
				</div>
			</section>

			{/* Content */}
			<section className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 pb-24">
				<div className="prose prose-zinc dark:prose-invert max-w-none prose-headings:font-[var(--font-bricolage)] prose-h2:text-2xl prose-h2:mt-12 prose-h2:mb-4 prose-p:text-zinc-600 dark:prose-p:text-zinc-400 prose-p:leading-relaxed prose-li:text-zinc-600 dark:prose-li:text-zinc-400">
					<div className="h-px bg-zinc-200 dark:bg-zinc-800 mb-12" />

					<h2>1. Acceptance of Terms</h2>
					<p>
						By accessing or using Galerique (&quot;the Platform&quot;), you agree to be bound by these Terms
						of Service. If you do not agree to these terms, please do not use our platform.
					</p>

					<h2>2. Description of Service</h2>
					<p>
						Galerique is a digital art gallery platform that allows artists to showcase, share, and sell
						their digital artwork. We provide tools for uploading, displaying, and discovering art in a
						curated environment.
					</p>

					<h2>3. Account Registration</h2>
					<p>To use certain features, you must create an account. You agree to:</p>
					<ul>
						<li>Provide accurate and complete registration information</li>
						<li>Maintain the security of your account credentials</li>
						<li>Promptly update any changes to your information</li>
						<li>Accept responsibility for all activities under your account</li>
					</ul>

					<h2>4. User Content</h2>
					<p>By uploading content to Galerique, you represent and warrant that:</p>
					<ul>
						<li>You are the original creator or have the necessary rights to the content</li>
						<li>Your content does not infringe on any third-party intellectual property rights</li>
						<li>Your content does not contain illegal, harmful, or offensive material</li>
						<li>
							You grant Galerique a non-exclusive license to display, distribute, and promote your content
							on the platform
						</li>
					</ul>

					<h2>5. Intellectual Property</h2>
					<p>
						Artists retain full ownership and copyright of their original works. Galerique does not claim
						ownership of any user-uploaded content. Our platform design, branding, and proprietary features
						are protected by intellectual property laws.
					</p>

					<h2>6. Purchases and Transactions</h2>
					<p>
						When purchasing artwork through Galerique, you agree to pay the listed price. All sales are
						subject to our refund policy. We act as an intermediary between buyers and artists and
						facilitate secure transactions.
					</p>

					<h2>7. Prohibited Conduct</h2>
					<p>You agree not to:</p>
					<ul>
						<li>Upload content you do not have rights to share</li>
						<li>Use the platform for any illegal purpose</li>
						<li>Harass, abuse, or threaten other users</li>
						<li>Attempt to gain unauthorized access to the platform or other accounts</li>
						<li>Scrape, copy, or redistribute content without permission</li>
						<li>Interfere with the proper functioning of the platform</li>
					</ul>

					<h2>8. Content Moderation</h2>
					<p>
						We reserve the right to review, remove, or restrict content that violates these terms or our
						community guidelines. Repeated violations may result in account suspension or termination.
					</p>

					<h2>9. Limitation of Liability</h2>
					<p>
						Galerique is provided &quot;as is&quot; without warranties of any kind. We are not liable for
						any indirect, incidental, or consequential damages arising from your use of the platform. Our
						total liability shall not exceed the amount you have paid to us in the preceding 12 months.
					</p>

					<h2>10. Termination</h2>
					<p>
						We may suspend or terminate your account at our discretion if you violate these terms. You may
						delete your account at any time. Upon termination, your right to use the platform ceases
						immediately.
					</p>

					<h2>11. Changes to Terms</h2>
					<p>
						We may modify these terms at any time. Continued use of the platform after changes constitutes
						acceptance of the updated terms. We will make reasonable efforts to notify users of significant
						changes.
					</p>

					<h2>12. Governing Law</h2>
					<p>
						These terms are governed by applicable law. Any disputes shall be resolved through appropriate
						legal channels.
					</p>

					<h2>13. Contact</h2>
					<p>
						For questions about these Terms of Service, please visit our{" "}
						<Link
							href="/about"
							className="text-zinc-900 dark:text-white underline underline-offset-4"
						>
							About page
						</Link>{" "}
						or review our{" "}
						<Link
							href="/privacy"
							className="text-zinc-900 dark:text-white underline underline-offset-4"
						>
							Privacy Policy
						</Link>
						.
					</p>
				</div>
			</section>
		</div>
	);
}
