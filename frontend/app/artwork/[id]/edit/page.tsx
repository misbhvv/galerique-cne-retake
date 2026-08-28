"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { ArrowLeft, GripVertical, Loader2, Save, Star, Trash2, Upload, X } from "lucide-react";
import ArtworkService from "@/src/services/artwork.service";
import { useAuth } from "@/src/context/AuthContext";
import { ArtworkDto, ArtworkImageDto, ArtworkUpdateDto } from "@/src/types";

const ART_VOCABULARY = [
	"abstract",
	"portrait",
	"landscape",
	"digital",
	"oil",
	"watercolor",
	"photography",
	"minimal",
	"surreal",
	"contemporary",
	"impressionist",
	"expressionist",
	"geometric",
	"figurative",
	"street-art",
	"illustration",
	"mixed-media",
	"collage",
	"black-and-white",
	"nature",
	"urban",
	"fantasy",
	"conceptual",
	"realism",
	"pop-art",
	"cinematic",
	"neon",
	"monochrome",
	"vintage",
	"modern",
];

function normalizeTag(input: string): string {
	return input.trim().toLowerCase().replace(/\s+/g, "-");
}

function suggestTags(title: string, description: string, currentTags: string[]): string[] {
	const text = `${title} ${description}`.toLowerCase();
	return ART_VOCABULARY.filter((tag) => {
		const spaced = tag.replace(/-/g, " ");
		return (text.includes(tag) || text.includes(spaced)) && !currentTags.includes(tag);
	}).slice(0, 8);
}

export default function EditArtworkPage() {
	const { id } = useParams();
	const router = useRouter();
	const { user, loading: authLoading } = useAuth();

	const artworkId = String(id);

	const [artwork, setArtwork] = useState<ArtworkDto | null>(null);
	const [title, setTitle] = useState("");
	const [description, setDescription] = useState("");
	const [price, setPrice] = useState("");
	const [tags, setTags] = useState<string[]>([]);
	const [tagInput, setTagInput] = useState("");
	const [loading, setLoading] = useState(true);
	const [saving, setSaving] = useState(false);
	const [busy, setBusy] = useState(false);
	const [error, setError] = useState<string | null>(null);
	const [draggedImageId, setDraggedImageId] = useState<string | null>(null);

	const images = useMemo(() => {
		if (!artwork?.images) return [];
		return [...artwork.images].sort((a, b) => a.sortOrder - b.sortOrder);
	}, [artwork?.images]);

	const suggestions = useMemo(() => suggestTags(title, description, tags), [title, description, tags]);

	const loadArtwork = async () => {
		if (!artworkId) return;
		setLoading(true);
		setError(null);
		try {
			const res = await ArtworkService.getById(artworkId);
			if (!res.ok) {
				setArtwork(null);
				setLoading(false);
				return;
			}
			const data: ArtworkDto = await res.json();
			setArtwork(data);
			setTitle(data.title || "");
			setDescription(data.description || "");
			setPrice(String(data.price ?? ""));
			setTags(data.tags ?? []);
		} catch {
			setError("Failed to load artwork");
		} finally {
			setLoading(false);
		}
	};

	useEffect(() => {
		void loadArtwork();
	}, [artworkId]);

	useEffect(() => {
		if (authLoading || !artwork) return;
		if (!user || user.id !== artwork.creator?.id) {
			router.replace(`/artwork/${artworkId}`);
		}
	}, [authLoading, user, artwork, artworkId, router]);

	const addTag = (raw: string) => {
		const cleaned = normalizeTag(raw);
		if (!cleaned || tags.includes(cleaned) || tags.length >= 10) {
			setTagInput("");
			return;
		}
		setTags((prev) => [...prev, cleaned]);
		setTagInput("");
	};

	const removeTag = (tag: string) => setTags((prev) => prev.filter((entry) => entry !== tag));

	const refreshArtwork = async () => {
		const res = await ArtworkService.getById(artworkId);
		if (res.ok) {
			const data: ArtworkDto = await res.json();
			setArtwork(data);
		}
	};

	const handleUploadImages = async (fileList: FileList | null) => {
		if (!fileList || fileList.length === 0) return;
		setBusy(true);
		setError(null);
		try {
			const files = Array.from(fileList);
			const response = await ArtworkService.uploadImages(artworkId, files);
			if (!response.ok) {
				throw new Error("Image upload failed");
			}
			await refreshArtwork();
		} catch (err: unknown) {
			setError(err instanceof Error ? err.message : "Image upload failed");
		} finally {
			setBusy(false);
		}
	};

	const handleSetMain = async (imageId: string) => {
		setBusy(true);
		setError(null);
		try {
			const response = await ArtworkService.setMainImage(artworkId, imageId);
			if (!response.ok) {
				throw new Error("Failed to set main image");
			}
			await refreshArtwork();
		} catch (err: unknown) {
			setError(err instanceof Error ? err.message : "Failed to set main image");
		} finally {
			setBusy(false);
		}
	};

	const handleDeleteImage = async (imageId: string) => {
		setBusy(true);
		setError(null);
		try {
			const response = await ArtworkService.deleteImage(artworkId, imageId);
			if (!response.ok) {
				throw new Error("Failed to delete image");
			}
			await refreshArtwork();
		} catch (err: unknown) {
			setError(err instanceof Error ? err.message : "Failed to delete image");
		} finally {
			setBusy(false);
		}
	};

	const handleReorderByIds = async (orderedImageIds: string[]) => {
		if (orderedImageIds.length !== images.length) return;
		setBusy(true);
		setError(null);
		try {
			const response = await ArtworkService.reorderImages(artworkId, { orderedImageIds });
			if (!response.ok) {
				throw new Error("Failed to reorder images");
			}
			await refreshArtwork();
		} catch (err: unknown) {
			setError(err instanceof Error ? err.message : "Failed to reorder images");
		} finally {
			setBusy(false);
		}
	};

	const handleDropOnImage = (targetImageId: string) => {
		if (!draggedImageId || draggedImageId === targetImageId) {
			setDraggedImageId(null);
			return;
		}

		const currentIds = images.map((image) => image.id);
		const from = currentIds.indexOf(draggedImageId);
		const to = currentIds.indexOf(targetImageId);
		if (from < 0 || to < 0) {
			setDraggedImageId(null);
			return;
		}

		const next = [...currentIds];
		next.splice(from, 1);
		next.splice(to, 0, draggedImageId);
		setDraggedImageId(null);
		void handleReorderByIds(next);
	};

	const handleSave = async () => {
		if (!artwork) return;
		setSaving(true);
		setError(null);
		try {
			const payload: ArtworkUpdateDto = {
				title: title.trim(),
				description: description.trim() || undefined,
				price: Number(price),
				tags,
			};
			const response = await ArtworkService.update(artwork.id, payload);
			if (!response.ok) {
				throw new Error("Failed to save artwork");
			}
			router.push(`/artwork/${artwork.id}`);
		} catch (err: unknown) {
			setError(err instanceof Error ? err.message : "Failed to save artwork");
		} finally {
			setSaving(false);
		}
	};

	if (loading || authLoading) {
		return (
			<div className="min-h-screen flex items-center justify-center">
				<div className="w-8 h-8 border border-stone-300 dark:border-stone-700 border-t-stone-900 dark:border-t-stone-100 rounded-full animate-spin" />
			</div>
		);
	}

	if (!artwork || !user || user.id !== artwork.creator?.id) {
		return null;
	}

	return (
		<div className="min-h-screen">
			<div className="max-w-5xl mx-auto px-6 lg:px-10 py-10">
				<Link
					href={`/artwork/${artwork.id}`}
					className="inline-flex items-center gap-2 text-sm text-stone-500 hover:text-stone-900 dark:hover:text-stone-100 mb-8"
				>
					<ArrowLeft
						size={15}
						strokeWidth={1.5}
					/>
					Back to artwork
				</Link>

				<h1 className="font-display text-3xl sm:text-4xl font-bold text-stone-900 dark:text-stone-100 mb-2">
					Edit mode
				</h1>
				<p className="text-sm text-stone-500 dark:text-stone-400 mb-8">
					Update details and manage image order in one place.
				</p>

				<div className="grid lg:grid-cols-[1.1fr_0.9fr] gap-8">
					<section className="rounded-xl border border-stone-200 dark:border-stone-800 p-5 bg-white/80 dark:bg-stone-950/60">
						<div className="flex items-center justify-between gap-4 mb-4">
							<p className="text-xs tracking-editorial text-stone-400 dark:text-stone-600">Images</p>
							<label className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-stone-900 dark:bg-stone-100 text-white dark:text-stone-900 text-xs font-medium cursor-pointer">
								<Upload
									size={12}
									strokeWidth={1.5}
								/>
								Upload
								<input
									type="file"
									multiple
									accept="image/jpeg,image/png,image/webp,image/jpg"
									onChange={(e) => handleUploadImages(e.target.files)}
									className="hidden"
									disabled={busy}
								/>
							</label>
						</div>

						<div className="space-y-2">
							{images.map((image: ArtworkImageDto, index) => (
								<div
									key={image.id}
									draggable={!busy}
									onDragStart={() => setDraggedImageId(image.id)}
									onDragOver={(e) => e.preventDefault()}
									onDrop={() => handleDropOnImage(image.id)}
									className="flex items-center justify-between gap-2 text-sm bg-stone-50 dark:bg-stone-900 rounded-md px-3 py-2.5"
								>
									<div className="flex items-center gap-2 truncate">
										<GripVertical
											size={14}
											strokeWidth={1.5}
											className="text-stone-400 cursor-grab"
										/>
										<span className="text-xs font-medium text-stone-500 dark:text-stone-400 w-5">
											{index + 1}
										</span>
										<span className="text-stone-700 dark:text-stone-300 truncate text-xs">
											{image.originalFileName}
										</span>
									</div>
									<div className="flex items-center gap-1">
										<button
											onClick={() => handleSetMain(image.id)}
											disabled={busy || image.isMainImage}
											className="p-1.5 rounded-md hover:bg-stone-200 dark:hover:bg-stone-700 disabled:opacity-30"
											title="Set as main"
										>
											<Star
												size={13}
												strokeWidth={1.5}
												className={
													image.isMainImage
														? "text-amber-500 fill-amber-500"
														: "text-stone-400"
												}
											/>
										</button>
										<button
											onClick={() => handleDeleteImage(image.id)}
											disabled={busy}
											className="p-1.5 rounded-md hover:bg-red-50 dark:hover:bg-red-900/20 text-stone-400 hover:text-red-500 disabled:opacity-30"
											title="Delete"
										>
											<Trash2
												size={13}
												strokeWidth={1.5}
											/>
										</button>
									</div>
								</div>
							))}
						</div>
					</section>

					<section className="rounded-xl border border-stone-200 dark:border-stone-800 p-5 bg-white/80 dark:bg-stone-950/60 space-y-5">
						<div>
							<label className="text-xs font-semibold tracking-widest uppercase text-stone-500">
								Title
							</label>
							<input
								value={title}
								onChange={(e) => setTitle(e.target.value)}
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
									placeholder="Add a tag"
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
							{suggestions.length > 0 && (
								<div className="mt-3 flex flex-wrap gap-2">
									{suggestions.map((suggestion) => (
										<button
											key={suggestion}
											type="button"
											onClick={() => addTag(suggestion)}
											className="px-2.5 py-1 rounded-full text-xs border border-dashed border-stone-300 dark:border-stone-700"
										>
											+ {suggestion}
										</button>
									))}
								</div>
							)}
							{tags.length > 0 && (
								<div className="mt-3 flex flex-wrap gap-2">
									{tags.map((tag) => (
										<span
											key={tag}
											className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs bg-stone-900 text-white dark:bg-stone-100 dark:text-stone-900"
										>
											{tag}
											<button
												type="button"
												onClick={() => removeTag(tag)}
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

						{error && <p className="text-sm text-red-500">{error}</p>}

						<button
							onClick={handleSave}
							disabled={saving || busy || !title.trim() || price.trim() === ""}
							className="w-full inline-flex items-center justify-center gap-2 rounded-full px-6 py-3 bg-stone-900 text-white dark:bg-stone-100 dark:text-stone-900 disabled:opacity-50"
						>
							{saving ? (
								<>
									<Loader2
										size={15}
										strokeWidth={1.8}
										className="animate-spin"
									/>
									Saving
								</>
							) : (
								<>
									<Save
										size={15}
										strokeWidth={1.8}
									/>
									Save changes
								</>
							)}
						</button>
					</section>
				</div>
			</div>
		</div>
	);
}
