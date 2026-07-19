import { PlayCircle, FileText, BookOpen, Puzzle } from "lucide-react";

const icons = {
  video: PlayCircle,
  pdf: FileText,
  reading: BookOpen,
  interactive: Puzzle,
};

function ContentTypeIcon({ type, className }) {
  const Icon = icons[type] || FileText;
  return <Icon className={className} />;
}

export default ContentTypeIcon;
