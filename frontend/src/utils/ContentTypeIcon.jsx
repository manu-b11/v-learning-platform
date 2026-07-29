import {
  PlayCircle,
  Headphones,
  FileText,
  Image,
  ClipboardCheck,
  Puzzle,
} from "lucide-react";

const icons = {
  VIDEO: PlayCircle,
  PODCAST: Headphones,
  PDF: FileText,
  IMAGE: Image,
  QUIZ: ClipboardCheck,
  SIMULATION: Puzzle,
};

function ContentTypeIcon({ type, className }) {
  const Icon = icons[type] || FileText;
  return <Icon className={className} />;
}

export default ContentTypeIcon;
