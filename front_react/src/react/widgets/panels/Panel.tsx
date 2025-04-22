import React from 'react';

interface PanelHeaderProps {
  children: React.ReactNode;
  className?: string; // Optional className for additional customization
}

const Panel: React.FC<PanelHeaderProps> = ({ children, className }) => {
  return (
    <div
      className={`p-4 bg-gray-200 border-gray-300 flex items-center justify-between ${className}`}
    >
      {children}
    </div>
  );
};

export default Panel;