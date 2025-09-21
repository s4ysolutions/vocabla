import React, { useState } from 'react';
import InputText from '../../widgets/inputs/InputText.tsx';

interface Props {
  onFilterChange: (filter: string) => void;
}

const ControlPanel: React.FC<Props> = ({ onFilterChange }) => {
  const [selectedLabels, setSelectedLabels] = useState<string[]>([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [activePlaceholderIndex, setActivePlaceholderIndex] = useState<number | null>(null);

  const availableLabels = ['Tag1', 'Tag2', 'Tag3', 'Tag4', 'Tag5', 'Tag6', 'Tag7'];

  const toggleLabel = (label: string) => {
    setSelectedLabels((prev) => {
      const updated = [...prev];
      if (activePlaceholderIndex !== null) {
        updated[activePlaceholderIndex] = label;
      }
      return updated.slice(0, 6);
    });
    setIsModalOpen(false);
    setActivePlaceholderIndex(null);
  };

  const removeLabel = (index: number) => {
    setSelectedLabels((prev) => {
      const updated = [...prev];
      updated[index] = ''; // Clear the label at the given index
      return updated;
    });
  };

  return (
    <div className="flex flex-col w-full h-full space-y-2">
      {/* Top Row: Search Field */}
      <div className="flex items-center justify-end space-x-4">
        {/* Input Field */}
        <InputText
          className="w-full max-w-xs"
          placeholder="Filter words..."
          onChange={(e) => onFilterChange(e.target.value)}
        />
      </div>

      {/* Bottom Row: Interactive Placeholders */}
      <div className="grid grid-cols-5 gap-2">
        {Array.from({ length: 5 }).map((_, index) => (
          <div
            key={index}
            className={`px-2 py-1 rounded border cursor-pointer ${
              selectedLabels[index]
                ? 'bg-blue-100 text-blue-700 border-blue-300'
                : 'bg-gray-100 text-gray-400 border-gray-300'
            }`}
            onClick={() =>
              selectedLabels[index]
                ? removeLabel(index) // Remove tag if filled
                : (setActivePlaceholderIndex(index), setIsModalOpen(true)) // Open modal if empty
            }
            style={{
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap',
              }}
          >
            {selectedLabels[index] || 'Empty'}
          </div>
        ))}
      </div>

      {/* Modal for Choosing Labels */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white p-6 rounded shadow-lg w-96">
            <h2 className="text-lg font-bold mb-4">Choose a Label</h2>
            <div className="grid grid-cols-2 gap-2">
              {availableLabels.map((label) => (
                <button
                  key={label}
                  className={`px-2 py-1 rounded ${
                    selectedLabels.includes(label)
                      ? 'bg-blue-500 text-white'
                      : 'bg-gray-200 text-gray-700'
                  }`}
                  onClick={() => toggleLabel(label)}
                >
                  {label}
                </button>
              ))}
            </div>
            <div className="mt-4 flex justify-end space-x-2">
              <button
                className="px-4 py-2 bg-gray-300 rounded hover:bg-gray-400"
                onClick={() => {
                  setIsModalOpen(false);
                  setActivePlaceholderIndex(null);
                }}
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ControlPanel;
