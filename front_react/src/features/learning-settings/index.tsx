import React from 'react';

const LearningSettings: React.FC = () => {
  return (
    <div className="h-full w-full flex flex-col overflow-auto md:overflow-hidden">
      {/* Top Section */}
      <div className="flex-1 md:overflow-y-auto">
        <h2 className="text-lg font-bold mb-2">Top Section</h2>
        <p>Content for the top section</p>
        <p>Additional content for the top sectionn...</p>
        <p>Additional content for the top sectionn...</p>
        <p>Additional content for the top sectionn...</p>
        <p>Additional content for the top sectionn...</p>
        <p>Additional content for the top sectionn...</p>
        <p>Additional content for the top sectionn...</p>
        <p>Additional content for the top sectionn...</p>
        <p>Additional content for the top sectionn...</p>
        <p>Additional content for the top sectionn...</p>
        <p>Additional content for the top sectionn...</p>
        <p>Additional content for the top sectionn...1</p>
        <p>Additional content for the top sectionn...0</p>
      </div>

      <div className='flex md:flex-1 flex-col md:flex-row md:overflow-y-hidden'>
        {/* Bottom Section */}
        <div className='md:overflow-y-auto h-full flex flex-col w-full md:w-1/2 '>
          <h2 className="text-lg font-bold mb-2">Bottom Left</h2>
          <p>Content for the bottom left section</p>
          <p>Additional content for the bottom left section...</p>
          <p>Additional content for the bottom left section...</p>
          <p>Additional content for the bottom left section...</p>
          <p>Additional content for the bottom left section...</p>
          <p>Additional content for the bottom left section...</p>
          <p>Additional content for the bottom left section...</p>
          <p>Additional content for the bottom left section...</p>
          <p>Additional content for the bottom left section...</p>
          <p>Additional content for the bottom left section...</p>
          <p>Additional content for the bottom left section...</p>
          <p>Additional content for the bottom left section...0</p>
        </div>
        <div className='md:overflow-y-auto h-full flex flex-col w-full md:w-1/2 '>
          <h2 className="text-lg font-bold mb-2">Bottom Right</h2>
          <p>Content for the bottom right section</p>
          <p>Additional content for the bottom right section...</p>
          <p>Additional content for the bottom right section...</p>
          <p>Additional content for the bottom right section...</p>
          <p>Additional content for the bottom right section...</p>
          <p>Additional content for the bottom right section...</p>
          <p>Additional content for the bottom right section...</p>
          <p>Additional content for the bottom right section...</p>
          <p>Additional content for the bottom right section...</p>
          <p>Additional content for the bottom right section...</p>
          <p>Additional content for the bottom right section...</p>
          <p>Additional content for the bottom right section...</p>
          <p>Additional content for the bottom right section...</p>
          <p>Additional content for the bottom right section...</p>
          <p>Additional content for the bottom right section...</p>
        </div>
      </div>
    </div >
  );
};

export default LearningSettings;