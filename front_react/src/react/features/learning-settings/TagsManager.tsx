import React, { useState } from 'react';
import useTags from './hooks/useTags.ts';

const TagsManager: React.FC = () => {
    const { tags, addTag, removeTag } = useTags();
    const [isAddPopupOpen, setIsAddPopupOpen] = useState(false);
    const [isDeletePopupOpen, setIsDeletePopupOpen] = useState(false);
    const [newTag, setNewTag] = useState('');
    const [tagToDelete, setTagToDelete] = useState<string | null>(null);

    const handleAddTag = () => {
        if (newTag.trim() && !tags.includes(newTag.trim())) {
            addTag(newTag.trim());
            setNewTag('');
            setIsAddPopupOpen(false);
        }
    };

    const handleDeleteTag = () => {
        if (tagToDelete) {
            removeTag(tagToDelete);
            setTagToDelete(null);
            setIsDeletePopupOpen(false);
        }
    };

    return (
        <div className="h-full flex flex-col space-y-4">
            {/* Add Tag Button */}
            <div className='flex justify-end'>
                <button
                    className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 min-w-64"
                    onClick={() => setIsAddPopupOpen(true)}
                >
                    Add Tag
                </button>
            </div>

            {/* Tags List */}
            <div className="flex flex-wrap gap-2 md:overflow-y-auto">
                {tags.map((tag) => (
                    <div
                        key={tag}
                        className="flex items-center space-x-2 px-3 py-1 bg-gray-200 rounded-full"
                    >
                        <span>{tag}</span>
                        <button
                            className="text-red-500 hover:text-red-700"
                            onClick={() => {
                                setTagToDelete(tag);
                                setIsDeletePopupOpen(true);
                            }}
                        >
                            ✕
                        </button>
                    </div>
                ))}
            </div>

            {/* Add Tag Popup */}
            {isAddPopupOpen && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
                    <div className="bg-white p-6 rounded shadow-lg w-96">
                        <h2 className="text-lg font-bold mb-4">Add a New Tag</h2>
                        <input
                            type="text"
                            value={newTag}
                            onChange={(e) => setNewTag(e.target.value)}
                            className="w-full px-3 py-2 border rounded mb-4"
                            placeholder="Enter tag name"
                        />
                        <div className="flex justify-end space-x-2">
                            <button
                                className="px-4 py-2 bg-gray-300 rounded hover:bg-gray-400"
                                onClick={() => setIsAddPopupOpen(false)}
                            >
                                Cancel
                            </button>
                            <button
                                className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
                                onClick={handleAddTag}
                            >
                                Add
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Delete Confirmation Popup */}
            {isDeletePopupOpen && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
                    <div className="bg-white p-6 rounded shadow-lg w-96">
                        <h2 className="text-lg font-bold mb-4">Confirm Deletion</h2>
                        <p>Are you sure you want to delete the tag "{tagToDelete}"?</p>
                        <div className="flex justify-end space-x-2 mt-4">
                            <button
                                className="px-4 py-2 bg-gray-300 rounded hover:bg-gray-400"
                                onClick={() => setIsDeletePopupOpen(false)}
                            >
                                Cancel
                            </button>
                            <button
                                className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600"
                                onClick={handleDeleteTag}
                            >
                                Delete
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default TagsManager;
