import { useState } from 'react';

const useTags = () => {
  const [tags, setTags] = useState<string[]>(['Tag1', 'Tag2', 'Tag3']);

  const addTag = (tag: string) => {
    setTags((prev) => [...prev, tag]);
  };

  const removeTag = (tag: string) => {
    setTags((prev) => prev.filter((t) => t !== tag));
  };

  return { tags, addTag, removeTag };
};

export default useTags;