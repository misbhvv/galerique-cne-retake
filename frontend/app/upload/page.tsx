"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { Check, GripVertical, Loader2, Plus, Star, X } from "lucide-react";
import ArtworkService from "@/src/services/artwork.service";
import { useAuth } from "@/src/context/AuthContext";
import { TagSuggestionDto } from "@/src/types";

const MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;
const ALLOWED_FILE_TYPES = new Set(["image/jpeg", "image/jpg", "image/png", "image/webp"]);

function normalizeTag(input: string): string {
	return input.trim().toLowerCase().replace(/\s+/g, "-");
}

export default function UploadPage() {
	const router = useRouter();
	const { user, loading } = useAuth();

	const [files, setFiles] = useState<File[]>([]);
	const [mainIndex, setMainIndex] = useState(0);
	const [selectedIndex, setSelectedIndex] = useState(0);
	const [title, setTitle] = useState("");
	const [description, setDescription] = useState("");
	const [price, setPrice] = useState("");
	const [year, setYear] = useState("");
	const [tags, setTags] = useState<string[]>([]);
	const [tagInput, setTagInput] = useState("");
	const [tagSuggestions, setTagSuggestions] = useState<TagSuggestionDto[]>([]);
	const [uploading, setUploading] = useState(false);
	const [error, setError] = useState<string | null>(null);
	const [dragOverIdx, setDragOverIdx] = useState<number | null>(null);
	const [isDropActive, setIsDropActive] = useState(false);
	const [allowNewTags, setAllowNewTags] = useState(false);
	const draggedIdx = useRef<number | null>(null);
	const fileInputRef = useRef<HTMLInputElement>(null);

	useEffect(() => {
		if (!loading && !user) {
			router.replace("/");
		}
	}, [loading, user, router]);

	const previews = useMemo(
		() =>
			files.map((file) => ({
				file,
				url: URL.createObjectURL(file),
			})),
		[files],
	);

	useEffect(() => {
		return () => previews.forEach((preview) => URL.revokeObjectURL(preview.url));
	}, [previews]);

	useEffect(() => {
		const q = [tagInput, title, description].filter(Boolean).join(" ").trim();
		const timeout = setTimeout(async () => {
			try {
				const res = await ArtworkService.tagSuggestions(q);
				if (!res.ok) return;
				const data: TagSuggestionDto[] = await res.json();
				setTagSuggestions(data);
			} catch {
				setTagSuggestions([]);
			}
		}, 220);
		return () => clearTimeout(timeout);
	}, [tagInput, title, description]);

	const addFiles = (incoming: FileList | null) => {
		if (!incoming) return;
		setError(null);

		const valid = Array.from(incoming).filter((file) => {
			if (!ALLOWED_FILE_TYPES.has(file.type)) {
				setError("Only JPG, PNG or WEBP images are allowed.");
				return false;
			}
			if (file.size > MAX_FILE_SIZE_BYTES) {
				setError("Each image must be at most 5MB.");
				return false;
			}
			return true;
		});
		if (!valid.length) return;

		setFiles((prev) => {
			const next = [...prev, ...valid].slice(0, 10);
			if (!prev.length && next.length) {
				setSelectedIndex(0);
				setMainIndex(0);
			}
			return next;
		});
	};

	const removeFile = (idx: number) => {
		setFiles((prev) => prev.filter((_, i) => i !== idx));
		setSelectedIndex((current) => {
			if (idx === current) return 0;
			if (idx < current) return current - 1;
			return current;
		});
		setMainIndex((current) => {
			if (idx === current) return 0;
			if (idx < current) return current - 1;
			return current;
		});
	};

	const handleDragStart = (idx: number) => {
		draggedIdx.current = idx;
	};

	const handleDragOver = (e: React.DragEvent, idx: number) => {
		e.preventDefault();
		setDragOverIdx(idx);
	};

	const handleDrop = (targetIdx: number) => {
		const from = draggedIdx.current;
		if (from === null || from === targetIdx) {
			draggedIdx.current = null;
			setDragOverIdx(null);
			return;
		}

		const next = [...files];
		const [moved] = next.splice(from, 1);
		next.splice(targetIdx, 0, moved);
		setFiles(next);

		if (mainIndex === from) setMainIndex(targetIdx);
		else if (from < mainIndex && targetIdx >= mainIndex) setMainIndex((current) => current - 1);
		else if (from > mainIndex && targetIdx <= mainIndex) setMainIndex((current) => current + 1);

		if (selectedIndex === from) setSelectedIndex(targetIdx);
		else if (from < selectedIndex && targetIdx >= selectedIndex) setSelectedIndex((current) => current - 1);
		else if (from > selectedIndex && targetIdx <= selectedIndex) setSelectedIndex((current) => current + 1);

		draggedIdx.current = null;
		setDragOverIdx(null);
	};

	const handleUploadZoneDragOver = (e: React.DragEvent<HTMLDivElement>) => {
		e.preventDefault();
		e.dataTransfer.dropEffect = "copy";
		setIsDropActive(true);
	};

	const handleUploadZoneDrop = (e: React.DragEvent<HTMLDivElement>) => {
		e.preventDefault();
		setIsDropActive(false);
		if (e.dataTransfer.files?.length) {
			addFiles(e.dataTransfer.files);
		}
	};

	const addTag = (raw: string) => {
		const cleaned = normalizeTag(raw);
		if (!cleaned || tags.includes(cleaned) || tags.length >= 10) {
			setTagInput("");
			return;
		}

		const knownTags = new Set(tagSuggestions.map((entry) => normalizeTag(entry.name)));
		if (!knownTags.has(cleaned) && !allowNewTags) {
			setError("Use an existing suggested tag first. Enable 'Allow new tags' only if needed.");
			return;
		}

		setTags((prev) => [...prev, cleaned]);
		setTagInput("");
		setError(null);
	};

	const removeTag = (tag: string) => setTags((prev) => prev.filter((entry) => entry !== tag));

	const canSubmit =
		files.length > 0 &&
		title.trim().length > 0 &&
		price.trim() !== "" &&
		Number(price) >= 0 &&
		(year.trim() === "" || (Number(year) >= 1000 && Number(year) <= 2100)) &&
		!uploading;

	const handleSubmit = async () => {
		if (!canSubmit) return;
		setUploading(true);
		setError(null);
		let createdArtworkId: string | null = null;

		try {
			const createRes = await ArtworkService.create({
				title: title.trim(),
				description: description.trim() || undefined,
				price: Number(price),
				year: year.trim() ? Number(year) : undefined,
				tags,
			});

			if (!createRes.ok) {
				const detail = await readErrorResponse(createRes, "Failed to create artwork");
				throw new Error(detail);
			}

			const artwork = await createRes.json();
			createdArtworkId = artwork.id;

			for (let i = 0; i < files.length; i += 1) {
				const uploadRes = await ArtworkService.uploadImages(artwork.id, [files[i]]);
				if (!uploadRes.ok) {
					const detail = await readErrorResponse(uploadRes, `Failed to upload image ${i + 1}`);
					throw new Error(detail);
				}
			}

			if (mainIndex > 0) {
				const imagesRes = await ArtworkService.listImages(artwork.id);
				if (imagesRes.ok) {
					const images = await imagesRes.json();
					const ordered = [...images].sort(
						(a: { sortOrder: number }, b: { sortOrder: number }) => a.sortOrder - b.sortOrder,
					);
					if (ordered[mainIndex]) {
						await ArtworkService.setMainImage(artwork.id, ordered[mainIndex].id);
					}
				}
			}

			router.push(`/artwork/${artwork.id}`);
		} catch (err: unknown) {
			if (createdArtworkId) {
				try {
					await ArtworkService.remove(createdArtworkId);
				} catch {
					/* ignore cleanup failure */
				}
			}
			if (err instanceof TypeError) {
				setError("Network error while uploading images. Check backend availability and CORS config.");
			} else {
				setError(err instanceof Error ? err.message : "Upload failed. Please try again.");
			}
		} finally {
			setUploading(false);
		}
	};

	const readErrorResponse = async (res: Response, fallback: string) => {
		try {
			const body = await res.json();
			if (body?.message && typeof body.message === "string") {
				return body.message;
			}
		} catch {
			/* ignore non-json body */
		}
		return `${fallback} (HTTP ${res.status})`;
	};

	if (loading || !user) return null;

	const selectedPreview = previews[selectedIndex]?.url;

	return (
		<div className="min-h-screen w-full">
			<div className="mx-auto px-6 lg:px-10 py-10">
				<div className="mb-8">
					<p className="tracking-editorial text-stone-400 dark:text-stone-600 mb-3">Create artwork</p>
					<h1 className="font-display text-3xl sm:text-4xl font-bold text-stone-900 dark:text-stone-100">
						Publish artwork
					</h1>
				</div>

				<div className="grid lg:grid-cols-[minmax(0,1.2fr)_minmax(0,0.8fr)] w-full gap-8">
					<section className="min-w-0 rounded-xl border border-stone-200 dark:border-stone-800 p-4 sm:p-5 bg-white dark:bg-stone-950">
						<div
							onDragEnter={() => setIsDropActive(true)}
							onDragOver={handleUploadZoneDragOver}
							onDragLeave={() => setIsDropActive(false)}
							onDrop={handleUploadZoneDrop}
							className={`rounded-lg overflow-hidden bg-stone-200 dark:bg-stone-800 h-[360px] sm:h-[440px] md:h-[500px] w-full flex items-center justify-center transition-colors ${
								isDropActive ? "ring-2 ring-stone-400 bg-stone-300 dark:bg-stone-700" : ""
							}`}
						>
							{selectedPreview ? (
								<img
									src={selectedPreview}
									alt="Selected artwork preview"
									className="w-full h-full object-contain"
								/>
							) : (
								<button
									type="button"
									onClick={() => fileInputRef.current?.click()}
									className="w-full h-full flex items-center justify-center text-sm text-stone-500 hover:text-stone-800 dark:hover:text-stone-200 cursor-pointer"
								>
									Drop your images here or click to add images
								</button>
							)}
						</div>

						<div className="mt-4 flex items-center gap-3 w-full min-w-0 max-w-full overflow-x-auto overflow-y-hidden pb-1">
							{previews.map((preview, idx) => (
								<div
									key={`${preview.file.name}-${idx}`}
									draggable
									onDragStart={() => handleDragStart(idx)}
									onDragOver={(e) => handleDragOver(e, idx)}
									onDrop={() => handleDrop(idx)}
									onDragEnd={() => {
										draggedIdx.current = null;
										setDragOverIdx(null);
									}}
									className={`relative shrink-0 w-24 h-24 rounded-md overflow-hidden border bg-stone-200 dark:bg-stone-800 transition-all ${
										selectedIndex === idx
											? "border-stone-900 dark:border-stone-100"
											: "border-stone-200 dark:border-stone-700"
									} ${dragOverIdx === idx ? "ring-2 ring-stone-400" : ""}`}
								>
									<button
										type="button"
										onClick={() => setSelectedIndex(idx)}
										className="w-full h-full"
									>
										<img
											src={preview.url}
											alt={preview.file.name}
											className="w-full h-full object-cover"
										/>
									</button>
									<div className="absolute top-1 left-1 flex gap-1">
										<button
											type="button"
											onClick={() => setMainIndex(idx)}
											className={`p-1 rounded-full bg-white/85 ${mainIndex === idx ? "text-amber-500" : "text-stone-500"}`}
											title="Set cover image"
										>
											<Star
												size={12}
												strokeWidth={1.9}
												fill={mainIndex === idx ? "currentColor" : "none"}
											/>
										</button>
										<span
											className="p-1 rounded-full bg-white/85 text-stone-500"
											title="Drag to reorder"
										>
											<GripVertical
												size={12}
												strokeWidth={1.6}
											/>
										</span>
									</div>
									<button
										type="button"
										onClick={() => removeFile(idx)}
										className="absolute top-1 right-1 p-1 rounded-full bg-white/85 text-stone-500 hover:text-red-500"
										title="Remove"
									>
										<X
											size={12}
											strokeWidth={2}
										/>
									</button>
								</div>
							))}

							<button
								type="button"
								onClick={() => fileInputRef.current?.click()}
								className="shrink-0 w-24 h-24 rounded-md border border-dashed border-stone-300 dark:border-stone-600 bg-stone-200 dark:bg-stone-800 flex items-center justify-center text-stone-500 hover:border-stone-500"
								title="Add image"
							>
								<Plus
									size={22}
									strokeWidth={1.8}
								/>
							</button>
						</div>

						<input
							ref={fileInputRef}
							type="file"
							aria-label="Upload artwork images"
							title="Upload artwork images"
							accept="image/jpeg,image/png,image/webp,image/jpg"
							multiple
							onChange={(e) => addFiles(e.target.files)}
							className="hidden"
						/>
					</section>

					<section className="min-w-0 rounded-xl border border-stone-200 dark:border-stone-800 p-5 bg-white dark:bg-stone-950 space-y-5">
						<div>
							<label className="text-xs font-semibold tracking-widest uppercase text-stone-500">
								Title
							</label>
							<input
								type="text"
								value={title}
								onChange={(e) => setTitle(e.target.value)}
								maxLength={100}
								placeholder="Artwork title"
								className="mt-2 w-full rounded-lg border border-stone-200 dark:border-stone-800 bg-white dark:bg-stone-900 px-4 py-3 text-sm"
							/>
						</div>

						<div>
							<label className="text-xs font-semibold tracking-widest uppercase text-stone-500">
								Description
							</label>
							<textarea
								value={description}
								onChange={(e) => setDescription(e.target.value)}
								rows={5}
								maxLength={500}
								placeholder="Tell the story behind your artwork"
								className="mt-2 w-full rounded-lg border border-stone-200 dark:border-stone-800 bg-white dark:bg-stone-900 px-4 py-3 text-sm resize-none"
							/>
						</div>

						<div>
							<label className="text-xs font-semibold tracking-widest uppercase text-stone-500">
								Price (EUR)
							</label>
							<input
								type="number"
								value={price}
								onChange={(e) => setPrice(e.target.value)}
								min="0"
								step="0.01"
								placeholder="0.00"
								className="mt-2 w-full rounded-lg border border-stone-200 dark:border-stone-800 bg-white dark:bg-stone-900 px-4 py-3 text-sm"
							/>
						</div>

						<div>
							<label className="text-xs font-semibold tracking-widest uppercase text-stone-500">
								Year (optional)
							</label>
							<input
								type="number"
								value={year}
								onChange={(e) => setYear(e.target.value)}
								min="1000"
								max="2100"
								placeholder="e.g. 1891"
								className="mt-2 w-full rounded-lg border border-stone-200 dark:border-stone-800 bg-white dark:bg-stone-900 px-4 py-3 text-sm"
							/>
						</div>

						<div>
							<label className="text-xs font-semibold tracking-widest uppercase text-stone-500">
								Tags
							</label>
							<div className="mt-2 flex gap-2">
								<input
									value={tagInput}
									onChange={(e) => setTagInput(e.target.value)}
									onKeyDown={(e) => {
										if ((e.key === "Enter" || e.key === ",") && tagInput.trim()) {
											e.preventDefault();
											addTag(tagInput);
										}
									}}
									placeholder="Type a tag"
									className="flex-1 rounded-lg border border-stone-200 dark:border-stone-800 bg-white dark:bg-stone-900 px-4 py-3 text-sm"
								/>
								<button
									type="button"
									onClick={() => addTag(tagInput)}
									className="px-4 py-3 rounded-lg bg-stone-100 dark:bg-stone-800 text-sm"
								>
									Add
								</button>
							</div>

							<div className="mt-2">
								<button
									type="button"
									onClick={() => setAllowNewTags((prev) => !prev)}
									className="text-[11px] text-stone-500 hover:text-stone-900 dark:hover:text-stone-100 underline underline-offset-2"
								>
									{allowNewTags ? "Only suggested tags" : "Allow new tags"}
								</button>
							</div>

							{tagSuggestions.length > 0 && (
								<div className="mt-3 flex flex-wrap gap-2">
									{tagSuggestions
										.filter((s) => !tags.includes(normalizeTag(s.name)))
										.slice(0, 8)
										.map((suggestion) => (
											<button
												key={suggestion.id}
												type="button"
												onClick={() => addTag(suggestion.name)}
												className="px-2.5 py-1 rounded-full text-xs border border-stone-200 dark:border-stone-700 text-stone-600 dark:text-stone-300 hover:border-stone-500"
												title={suggestion.description || suggestion.name}
											>
												+ {suggestion.name}
											</button>
										))}
								</div>
							)}

							{tags.length > 0 && (
								<div className="mt-3 flex flex-wrap gap-2">
									{tags.map((tag) => (
										<span
											key={tag}
											className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs border border-stone-300 dark:border-stone-700 text-stone-700 dark:text-stone-200"
										>
											{tag}
											<button
												type="button"
												onClick={() => removeTag(tag)}
												aria-label={`Remove tag ${tag}`}
												title={`Remove tag ${tag}`}
											>
												<X
													size={10}
													strokeWidth={2}
												/>
											</button>
										</span>
									))}
								</div>
							)}
						</div>

						{error && (
							<div className="p-3 rounded-lg bg-red-50 dark:bg-red-900/10 border border-red-200 dark:border-red-900/30 text-red-600 dark:text-red-400 text-sm">
								{error}
							</div>
						)}

						<button
							onClick={handleSubmit}
							disabled={!canSubmit}
							className="w-full flex items-center justify-center gap-2 rounded-full px-6 py-3.5 bg-stone-900 text-white dark:bg-stone-100 dark:text-stone-900 text-sm font-semibold disabled:opacity-50"
						>
							{uploading ? (
								<>
									<Loader2
										size={16}
										strokeWidth={1.8}
										className="animate-spin"
									/>
									Publishing
								</>
							) : (
								<>
									<Check
										size={16}
										strokeWidth={2}
									/>
									Publish artwork
								</>
							)}
						</button>
					</section>
				</div>
			</div>
		</div>
	);
}
